package io.kuberig.core.deployment

import io.kuberig.annotations.ApplyAction
import io.kuberig.config.ClientSideApplyFlags
import io.kuberig.config.KubeRigFlags
import io.kuberig.config.ServerSideApplyFlags
import io.kuberig.core.deployment.control.TickInfo
import io.kuberig.core.deployment.control.TickSystemControl
import io.kuberig.core.preparation.ResourceUrlInfo
import io.kuberig.core.resource.RawJsonResourceInfo
import io.kuberig.fs.EnvironmentFileSystem

class ResourceDeployer(
    flags: KubeRigFlags,
    environmentFileSystem: EnvironmentFileSystem,
    deployControl: TickInfo,
    resourceGenerationRuntimeClasspathClassLoader: ClassLoader
) {

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
            tickSystemControl.execute(deploymentPlan, this::execute)
        } finally {
            apiServerIntegration.shutDown()
        }

        if (!statusTrackingDeploymentListener.success) {
            throw DeploymentFailure(statusTrackingDeploymentListener.failureMessage)
        }
    }

    private fun execute(deploymentTask: DeploymentTask): Boolean {
        val rawResourceInfo = deploymentTask.rawJsonResourceInfo

        return applyStrategy.applyResource(
            rawResourceInfo,
            rawResourceInfo.resourceUrlInfo,
            deploymentTask.applyAction
        )
    }

}

abstract class ApplyStrategy<out F>(
    val name: String,
    val apiServerIntegration: ApiServerIntegration,
    val flags: F,
    val deploymentListener: DeploymentListener
) {

    fun applyResource(rawJsonResourceInfo: RawJsonResourceInfo, resourceUrlInfo: ResourceUrlInfo, applyAction: ApplyAction): Boolean {
        deploymentListener.deploymentStart(rawJsonResourceInfo, resourceUrlInfo)

        return when (val getResourceResult = apiServerIntegration.getResource(resourceUrlInfo)) {
            is UnknownGetResourceResult -> {
                createResource(rawJsonResourceInfo, resourceUrlInfo)
            }
            is ExistsGetResourceResult -> {
                when(applyAction) {
                    ApplyAction.CREATE_ONLY -> {
                        deploymentListener.deploymentSuccess(rawJsonResourceInfo, getResourceResult)
                        return true
                    }
                    ApplyAction.CREATE_OR_UPDATE -> {
                        updateResource(rawJsonResourceInfo, resourceUrlInfo, getResourceResult)
                    }
                    ApplyAction.RECREATE -> {
                        return when(val deleteResult = apiServerIntegration.deleteResource(resourceUrlInfo)) {
                            is FailedDeleteResourceResult -> {
                                deploymentListener.deploymentFailure(rawJsonResourceInfo, deleteResult)
                                false
                            }
                            is SuccessDeleteResourceResult -> {
                                createResource(rawJsonResourceInfo, resourceUrlInfo)
                            }
                        }
                    }
                }
            }
        }
    }

    protected abstract fun createResource(rawJsonResourceInfo: RawJsonResourceInfo, resourceUrlInfo: ResourceUrlInfo): Boolean

    protected abstract fun updateResource(
        rawJsonResourceInfo: RawJsonResourceInfo,
        resourceUrlInfo: ResourceUrlInfo,
        getResourceResult: ExistsGetResourceResult
    ): Boolean
}

class DeploymentFailure(message: String) : Exception(message)