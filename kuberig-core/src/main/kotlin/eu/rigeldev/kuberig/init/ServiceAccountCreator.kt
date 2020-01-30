package eu.rigeldev.kuberig.init

import eu.rigeldev.kuberig.cluster.client.*
import eu.rigeldev.kuberig.config.KubeRigFlags
import eu.rigeldev.kuberig.fs.EnvironmentFileSystem
import eu.rigeldev.kuberig.kubectl.OkContextResult
import org.json.JSONArray
import org.json.JSONObject

class ServiceAccountCreator(private val flags: KubeRigFlags) {

    private val serviceAccountBasics = KindBasics("v1", "ServiceAccount", "serviceaccounts")
    private val roleBindingBasics = KindBasics("rbac.authorization.k8s.io/v1", "RoleBinding", "rolebindings")
    private val secretBasics = KindBasics("v1", "Secret", "secrets")

    fun createDefaultServiceAccount(
        contextResult: OkContextResult,
        environmentFileSystem: EnvironmentFileSystem,
        serviceAccountName: String
    ) {
        val clusterInteractionService = ClusterInteractionService(
            contextResult.clusterDetail.server,
            flags,
            contextResult.clusterDetail.certificateAuthorityData,
            contextResult.authDetails
        )

        var accessTokenStored = false
        val namespace = contextResult.namespace

        try {
            var readResult = this.readServiceAccount(clusterInteractionService, namespace, serviceAccountName)

            val serviceAccountAvailable : Boolean = if (readResult is NotFoundResourceReadResult) {
                val continueCreation = this.createServiceAccountIfNeeded(clusterInteractionService, namespace, serviceAccountName)

                if (continueCreation) {
                    this.createRoleBinding(clusterInteractionService, namespace, serviceAccountName)
                } else {
                    false
                }
            } else {
                true
            }

            if (serviceAccountAvailable) {
                readResult = this.readServiceAccount(clusterInteractionService, namespace, serviceAccountName)
            }

            if (readResult is FoundResourceReadResult) {
                val accessToken = this.readServiceAccountAccessToken(clusterInteractionService, namespace, readResult.resourceBody)

                if (accessToken != null) {
                    environmentFileSystem.storeAccessToken(accessToken)
                    accessTokenStored = true
                }
            }

        } finally {
            clusterInteractionService.shutdown()
        }

        if (accessTokenStored) {
            println("[SUCCESS] Access-token for service account $serviceAccountName, stored in environment directory.")
        } else {
            println("[FAILURE] Failed to store access-token $serviceAccountName in environment directory, please check previous error messages for clues.")
        }
    }

    private fun readServiceAccount(
        clusterInteractionService: ClusterInteractionService,
        namespace: String,
        serviceAccountName: String
    ): ResourceReadResult {
        return clusterInteractionService.read(serviceAccountBasics, namespace, serviceAccountName)
    }

    private fun createServiceAccountIfNeeded(
        clusterInteractionService: ClusterInteractionService,
        namespace: String,
        serviceAccountName: String
    ): Boolean {
        var continueCreation = true

        when (clusterInteractionService.read(serviceAccountBasics, namespace, serviceAccountName)) {
            is FoundResourceReadResult -> {
                println("ServiceAccount $serviceAccountName, already exists in namespace $namespace no need to create it.")
                continueCreation = false
            }
            is NotFoundResourceReadResult -> {
                val serviceAccountObject = serviceAccountBasics.startObject(namespace, serviceAccountName)

                when (val saCreateResult = clusterInteractionService.create(serviceAccountBasics, namespace, serviceAccountObject)) {
                    is ResourceCreateSuccessResult -> {
                        println("Created $serviceAccountName service account in namespace $namespace.")
                    }
                    is ResourceCreateFailedResult -> {
                        println("Failed to create service account $serviceAccountName in namespace $namespace.")
                        printCreateFailureInfo(saCreateResult)
                        continueCreation = false
                    }
                }
            }
        }

        return continueCreation
    }

    private fun createRoleBinding(
        clusterInteractionService: ClusterInteractionService,
        namespace: String,
        serviceAccountName: String
    ): Boolean {
        var continueCreation = true

        val rb = roleBindingBasics.startObject(namespace, "$serviceAccountName-edit")
            .put(
                "roleRef",
                JSONObject()
                    .put("apiGroup", "rbac.authorization.k8s.io")
                    .put("kind", "ClusterRole")
                    .put("name", "edit")
            )
            .put(
                "subjects",
                JSONArray(
                    arrayOf(
                        JSONObject()
                            .put("kind", serviceAccountBasics.kind)
                            .put("name", serviceAccountName)
                            .put("namespace", namespace)
                    )
                )
            )

        when (val rbCreateResult = clusterInteractionService.create(roleBindingBasics, namespace, rb)) {
            is ResourceCreateFailedResult -> {
                println("Failed to create edit role binding for service account $serviceAccountName in namespace $namespace.")
                printCreateFailureInfo(rbCreateResult)
                continueCreation = false
            }
        }

        return continueCreation
    }

    private fun readServiceAccountAccessToken(
        clusterInteractionService: ClusterInteractionService,
        namespace: String,
        serviceAccountObject: JSONObject
    ): String? {
        val secretName = serviceAccountObject.getJSONArray("secrets").getJSONObject(0).getString("name")

        val secretReadResult = clusterInteractionService.read(secretBasics, namespace, secretName)

        return if (secretReadResult is FoundResourceReadResult) {
            secretReadResult.resourceBody.getJSONObject("data").getString("token")
        } else {
            println("Secret $secretName not found in namespace $namespace")
            null
        }
    }

    private fun printCreateFailureInfo(createFailureResult: ResourceCreateFailedResult) {
        println(createFailureResult.status)
        println(createFailureResult.statusText)
        println(createFailureResult.body.toString())
    }
}