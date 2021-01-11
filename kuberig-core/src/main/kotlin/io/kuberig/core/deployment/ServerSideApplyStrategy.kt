package io.kuberig.core.deployment

import io.kuberig.config.ServerSideApplyFlags
import io.kuberig.core.resource.RawResourceInfo

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

    override fun createResource(rawResourceInfo: RawResourceInfo, resourceUrlInfo: ResourceUrlInfo): Boolean {
        return apply(rawResourceInfo, resourceUrlInfo)
    }

    override fun updateResource(
        rawResourceInfo: RawResourceInfo,
        resourceUrlInfo: ResourceUrlInfo,
        getResourceResult: ExistsGetResourceResult
    ): Boolean {
        return apply(rawResourceInfo, resourceUrlInfo)
    }

    private fun apply(rawResourceInfo: RawResourceInfo, resourceUrlInfo: ResourceUrlInfo): Boolean {
        val patchResult = apiServerIntegration.serverSideApplyResourcePatch(
            rawResourceInfo,
            resourceUrlInfo,
            flags.force
        )

        return when (patchResult) {
            is FailedServerSideApplyPatchResourceResult -> {
                deploymentListener.deploymentFailure(rawResourceInfo, patchResult)
                false
            }
            is SuccessServerSideApplyPatchResourceResult -> {
                deploymentListener.deploymentSuccess(rawResourceInfo, patchResult)
                true
            }
        }
    }
}