package io.kuberig.core.deployment

import io.kuberig.annotations.ApplyAction
import io.kuberig.config.ClientSideApplyFlags
import io.kuberig.config.KubeRigFlags
import io.kuberig.config.ServerSideApplyFlags
import io.kuberig.core.deployment.control.TickInfo
import io.kuberig.core.deployment.control.TickSystemControl
import io.kuberig.core.resource.RawResourceInfo
import io.kuberig.fs.EnvironmentFileSystem

class ResourceDeployer(
    flags: KubeRigFlags,
    environmentFileSystem: EnvironmentFileSystem,
    deployControl: TickInfo,
    resourceGenerationRuntimeClasspathClassLoader: ClassLoader
) {

    private val apiServerUrl: String = environmentFileSystem.apiServerUrl()
    private val defaultNamespace: String = environmentFileSystem.defaultNamespace()

    private val apiServerIntegration: ApiServerIntegration = ApiServerIntegration(
        environmentFileSystem.certificateAuthorityData(),
        environmentFileSystem.readAuthDetails(),
        flags
    )
    private val applyStrategy: ApplyStrategy<Any> = when (val applyStrategyFlags = flags.applyStrategyFlags) {
        is ServerSideApplyFlags -> {
            ServerSideApplyStrategy(apiServerIntegration, applyStrategyFlags, DeploymentListenerRegistry)
        }
        is ClientSideApplyFlags -> {
            ClientSideApplyStrategy(apiServerIntegration, applyStrategyFlags, DeploymentListenerRegistry)
        }
    }
    private val tickSystemControl = TickSystemControl(
        deployControl,
        resourceGenerationRuntimeClasspathClassLoader,
        DeploymentListenerRegistry
    )
    private val statusTrackingDeploymentListener = StatusTrackingDeploymentListener()

    init {
        DeploymentListenerRegistry.reset()

        DeploymentListenerRegistry.registerListener(ProgressReportingDeploymentListener(applyStrategy))
        DeploymentListenerRegistry.registerListener(statusTrackingDeploymentListener)
    }

    fun execute(deploymentPlan: DeploymentPlan) {
        try {
            tickSystemControl.execute(deploymentPlan) { deploymentTask ->
                execute(deploymentTask)
            }
        } finally {
            apiServerIntegration.shutDown()
        }

        if (!statusTrackingDeploymentListener.success) {
            throw DeploymentFailure(statusTrackingDeploymentListener.failureMessage)
        }
    }

    private fun execute(deploymentTask: DeploymentTask): Boolean {
        val rawResourceInfo = deploymentTask.rawResourceInfo

        val apiResources = ApiResources(
            apiServerIntegration,
            apiServerUrl,
            rawResourceInfo.apiVersion
        )
        val resourceUrlInfo = apiResources.resourceUrl(rawResourceInfo)

        return applyStrategy.applyResource(rawResourceInfo, resourceUrlInfo, deploymentTask.applyAction)
    }

}

abstract class ApplyStrategy<out F>(
    val name: String,
    val apiServerIntegration: ApiServerIntegration,
    val flags: F,
    val deploymentListener: DeploymentListener
) {

    fun applyResource(rawResourceInfo: RawResourceInfo, resourceUrlInfo: ResourceUrlInfo, applyAction: ApplyAction): Boolean {
        deploymentListener.deploymentStart(rawResourceInfo, resourceUrlInfo)

        return when (val getResourceResult = apiServerIntegration.getResource(resourceUrlInfo)) {
            is UnknownGetResourceResult -> {
                createResource(rawResourceInfo, resourceUrlInfo)
            }
            is ExistsGetResourceResult -> {
                when(applyAction) {
                    ApplyAction.CREATE_ONLY -> {
                        deploymentListener.deploymentSuccess(rawResourceInfo, getResourceResult)
                        return true
                    }
                    ApplyAction.CREATE_OR_UPDATE -> {
                        updateResource(rawResourceInfo, resourceUrlInfo, getResourceResult)
                    }
                    ApplyAction.RECREATE -> {
                        return when(val deleteResult = apiServerIntegration.deleteResource(resourceUrlInfo)) {
                            is FailedDeleteResourceResult -> {
                                deploymentListener.deploymentFailure(rawResourceInfo, deleteResult)
                                false
                            }
                            is SuccessDeleteResourceResult -> {
                                createResource(rawResourceInfo, resourceUrlInfo)
                            }
                        }
                    }
                }
            }
        }
    }

    protected abstract fun createResource(rawResourceInfo: RawResourceInfo, resourceUrlInfo: ResourceUrlInfo): Boolean

    protected abstract fun updateResource(
        rawResourceInfo: RawResourceInfo,
        resourceUrlInfo: ResourceUrlInfo,
        getResourceResult: ExistsGetResourceResult
    ): Boolean
}

class DeploymentFailure(message: String) : Exception(message)