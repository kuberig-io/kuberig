package eu.rigeldev.kuberig.init

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import eu.rigeldev.kuberig.cluster.client.ClusterClientBuilder
import eu.rigeldev.kuberig.config.KubeRigFlags
import eu.rigeldev.kuberig.fs.EnvironmentFileSystem
import eu.rigeldev.kuberig.kubectl.OkContextResult
import kong.unirest.Unirest
import org.json.JSONArray
import org.json.JSONObject

class ServiceAccountCreator(private val flags: KubeRigFlags) {

    fun createDefaultServiceAccount(contextResult: OkContextResult, environmentFileSystem: EnvironmentFileSystem) {
        val objectMapper = ObjectMapper()
        objectMapper.findAndRegisterModules()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

        val unirestInstance = Unirest.spawnInstance()
        try {
            ClusterClientBuilder(flags, objectMapper, unirestInstance)
                .initializeClient(contextResult.clusterDetail.certificateAuthorityData, contextResult.authDetails)

            val apiServer = contextResult.clusterDetail.server

            val serviceAccountName = "kuberig"
            val namespace = contextResult.namespace

            // create service account
            // POST - https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/api/v1/namespaces/default/serviceaccounts
            // {"apiVersion":"v1","kind":"ServiceAccount","metadata":{"creationTimestamp":null,"name":"kuberig-deployer"}}
            val sa = JSONObject()
                .put("apiVersion", "v1")
                .put("kind", "ServiceAccount")
                .put(
                    "metadata", JSONObject()
                        .put("name", serviceAccountName)
                        .put("namespace", namespace)
                )
            val saPost = unirestInstance.post("$apiServer/api/v1/namespaces/$namespace/serviceaccounts")
                .header("Content-Type", "application/json")
                .body(sa)
                .asJson()

            if (saPost.status == 201) {
                println("Created $serviceAccountName service account in namespace $namespace.")

                // create role binding (edit)
                // POST POST https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/apis/rbac.authorization.k8s.io/v1/namespaces/default/rolebindings
                // {"apiVersion":"rbac.authorization.k8s.io/v1","kind":"RoleBinding","metadata":{"creationTimestamp":null,"name":"kuberig-deployer-edit"},"roleRef":{"apiGroup":"rbac.authorization.k8s.io","kind":"ClusterRole","name":"edit"},"subjects":[{"kind":"ServiceAccount","name":"kuberig-deployer","namespace":"default"}

                val rb = JSONObject()
                    .put("apiVersion", "rbac.authorization.k8s.io/v1")
                    .put("kind", "RoleBinding")
                    .put(
                        "metadata",
                        JSONObject()
                            .put("name", "$serviceAccountName-edit")
                            .put("namespace", namespace)
                    )
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
                                    .put("kind", "ServiceAccount")
                                    .put("name", serviceAccountName)
                                    .put("namespace", namespace)
                            )
                        )

                    )

                val rbPost =
                    unirestInstance.post("$apiServer/apis/rbac.authorization.k8s.io/v1/namespaces/$namespace/rolebindings")
                        .header("Content-Type", "application/json")
                        .body(rb)
                        .asJson()

                if (rbPost.status == 201) {
                    println("Created edit role binding for $serviceAccountName service account in namespace $namespace")

                    // describe sa
                    // GET https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/api/v1/namespaces/default/serviceaccounts/kuberig-deployer
                    // {"kind":"ServiceAccount","apiVersion":"v1","metadata":{"name":"kuberig-deployer","namespace":"default","selfLink":"/api/v1/namespaces/default/serviceaccounts/kuberig-deployer","uid":"c1d6e5eb-938e-11e9-8593-3a05544fda29","resourceVersion":"128506","creationTimestamp":"2019-06-20T19:08:15Z"},"secrets":[{"name":"kuberig-deployer-token-m4kdm"}]}
                    val saDescGet =
                        unirestInstance.get("$apiServer/api/v1/namespaces/$namespace/serviceaccounts/$serviceAccountName")
                            .asJson()

                    val saDesc = saDescGet.body.getObject()
                    val secretName = saDesc.getJSONArray("secrets").getJSONObject(0).getString("name")

                    // describe secret
                    // GET https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/api/v1/namespaces/default/secrets/kuberig-deployer-token-m4kdm
                    val secretGet = unirestInstance.get("$apiServer/api/v1/namespaces/$namespace/secrets/$secretName")
                        .asJson()

                    val token = secretGet.body.getObject().getJSONObject("data").getString("token")

                    environmentFileSystem.storeAccessToken(token)

                } else {
                    println("Failed to create edit role binding")
                }
            } else {
                println("Failed to create service account")
                println(saPost.status)
                println(saPost.statusText)
                println(saPost.body.toString())
            }


            // describe sa
            // GET https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/api/v1/namespaces/default/serviceaccounts/kuberig-deployer
            // {"kind":"ServiceAccount","apiVersion":"v1","metadata":{"name":"kuberig-deployer","namespace":"default","selfLink":"/api/v1/namespaces/default/serviceaccounts/kuberig-deployer","uid":"c1d6e5eb-938e-11e9-8593-3a05544fda29","resourceVersion":"128506","creationTimestamp":"2019-06-20T19:08:15Z"},"secrets":[{"name":"kuberig-deployer-token-m4kdm"}]}

            // describe secret
            // GET https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/api/v1/namespaces/default/secrets/kuberig-deployer-token-m4kdm
            //
        }
        finally {
            unirestInstance.shutDown()
        }
    }

}