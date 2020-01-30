package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.init.ServiceAccountCreator
import eu.rigeldev.kuberig.kubectl.ErrorContextResult
import eu.rigeldev.kuberig.kubectl.KubectlConfigReader
import eu.rigeldev.kuberig.kubectl.OkContextResult
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class InitEnvironmentTask: AbstractKubeRigTask() {

    private var environmentName = ""
    private var apiServerUrl = ""
    private var currentKubectlContext = false

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

                    serviceAccountCreator.createDefaultServiceAccount(contextResult, environmentFileSystem)

                    environmentFileSystem.storeClusterCertificateAuthorityData(contextResult.clusterDetail.certificateAuthorityData)
                    environmentFileSystem.initConfigsFile(contextResult.clusterDetail.server)
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

            environmentFileSystem.initConfigsFile(this.apiServerUrl)
        }
    }
}