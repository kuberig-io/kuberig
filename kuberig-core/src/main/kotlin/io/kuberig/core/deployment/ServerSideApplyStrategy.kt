package io.kuberig.core.deployment

import io.kuberig.config.ServerSideApplyFlags

/**
 * Creates/updates resources using server-side-apply.
 */
class ServerSideApplyStrategy(
    apiServerIntegration: ApiServerIntegration,
    flags: ServerSideApplyFlags,
    deploymentListener: DeploymentListener
) : ApplyStrategy<ServerSideApplyFlags>(
    "server-side-apply",
    apiServerIntegration,
    flags,
    deploymentListener
) {

    override fun createResource(newResourceInfo: NewResourceInfo, resourceUrlInfo: ResourceUrlInfo): Boolean {
        return apply(newResourceInfo, resourceUrlInfo)
    }

    override fun updateResource(
        newResourceInfo: NewResourceInfo,
        resourceUrlInfo: ResourceUrlInfo,
        getResourceResult: ExistsGetResourceResult
    ): Boolean {
        return apply(newResourceInfo, resourceUrlInfo)
    }

    private fun apply(newResourceInfo: NewResourceInfo, resourceUrlInfo: ResourceUrlInfo): Boolean {
        val patchResult = apiServerIntegration.serverSideApplyResourcePatch(
            newResourceInfo,
            resourceUrlInfo,
            flags.force
        )

        return when (patchResult) {
            is FailedServerSideApplyPatchResourceResult -> {
                deploymentListener.deploymentFailure(newResourceInfo, patchResult)
                false
            }
            is SuccessServerSideApplyPatchResourceResult -> {
                deploymentListener.deploymentSuccess(newResourceInfo, patchResult)
                true
            }
        }
    }
}