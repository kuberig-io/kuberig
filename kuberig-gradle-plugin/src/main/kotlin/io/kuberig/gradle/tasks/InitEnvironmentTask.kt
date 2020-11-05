package io.kuberig.gradle.tasks

import io.kuberig.init.ServiceAccountCreator
import io.kuberig.kubectl.ErrorContextResult
import io.kuberig.kubectl.KubectlConfigReader
import io.kuberig.kubectl.OkContextResult
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class InitEnvironmentTask: AbstractKubeRigTask() {

    private var environmentName = ""
    private var apiServerUrl = ""
    private var currentKubectlContext = false
    private var serviceAccount = "kuberig"
    private var defaultNamespace = "default"

    @Input
    fun getEnvironmentName(): String {
        return this.environmentName
    }

    @Input
    fun getApiServerUrl(): String {
        return this.apiServerUrl
    }

    @Option(option = "name", description = "The name of the environment that you want to create")
    fun setEnvironmentName(environmentName: String) {
        this.environmentName= environmentName
    }

    @Option(option = "apiServerUrl", description = "The URL of the api server of your cluster")
    fun setApiServerUrl(apiServerUrl: String) {
        this.apiServerUrl = apiServerUrl
    }

    @Option(option = "currentKubectlContext", description = "From the current kubectl context")
    fun setCurrentKubectlContext(currentKubectlContext: Boolean) {
        this.currentKubectlContext = currentKubectlContext
    }

    @Option(option = "serviceAccount", description = "Allows you to overwrite the default service account name (kuberig)")
    fun setServiceAccount(serviceAccount: String) {
        this.serviceAccount = serviceAccount
    }

    @Option(option = "defaultNamespace", description = "Allows you to specify the default namespace (ignored when --currentKubectlContext flag is used)")
    fun setDefaultNamespace(defaultNamespace: String) {
        this.defaultNamespace = defaultNamespace
    }

    @TaskAction
    fun createEnvironment() {

        if (this.environmentName == "") {
            println("--name is required")
            return
        }

        val rootFileSystem = this.kubeRigExtension().rootFileSystem()
        val environmentFileSystem = rootFileSystem.environment(this.environmentName)

        environmentFileSystem.init()

        if (currentKubectlContext) {

            val kubectlConfigReader = KubectlConfigReader()

            when (val contextResult = kubectlConfigReader.readKubectlConfig()) {
                is OkContextResult -> {
                    val serviceAccountCreator = ServiceAccountCreator(kubeRigExtension().flags)

                    serviceAccountCreator.createDefaultServiceAccount(contextResult, environmentFileSystem, this.serviceAccount)

                    environmentFileSystem.storeClusterCertificateAuthorityData(contextResult.clusterDetail.certificateAuthorityData)
                    environmentFileSystem.initConfigsFile(contextResult.clusterDetail.server, contextResult.namespace, this.serviceAccount)
                }
                is ErrorContextResult -> {
                    println("Failed to read current kubectl context:[error]${contextResult.error}")
                }
            }

        } else {
            if (this.apiServerUrl == "") {
                println("--apiServerUrl is required")
                return
            }

            environmentFileSystem.initConfigsFile(this.apiServerUrl, this.defaultNamespace, this.serviceAccount)
        }
    }
}