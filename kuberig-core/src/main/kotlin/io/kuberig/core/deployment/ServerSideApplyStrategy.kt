package io.kuberig.core.deployment

import io.kuberig.config.ServerSideApplyFlags
import io.kuberig.core.preparation.ResourceUrlInfo
import io.kuberig.core.resource.RawJsonResourceInfo

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

    override fun createResource(rawJsonResourceInfo: RawJsonResourceInfo, resourceUrlInfo: ResourceUrlInfo): Boolean {
        return apply(rawJsonResourceInfo, resourceUrlInfo)
    }

    override fun updateResource(
        rawJsonResourceInfo: RawJsonResourceInfo,
        resourceUrlInfo: ResourceUrlInfo,
        getResourceResult: ExistsGetResourceResult
    ): Boolean {
        return apply(rawJsonResourceInfo, resourceUrlInfo)
    }

    private fun apply(rawJsonResourceInfo: RawJsonResourceInfo, resourceUrlInfo: ResourceUrlInfo): Boolean {
        val patchResult = apiServerIntegration.serverSideApplyResourcePatch(
            rawJsonResourceInfo,
            resourceUrlInfo,
            flags.force
        )

        return when (patchResult) {
            is FailedServerSideApplyPatchResourceResult -> {
                deploymentListener.deploymentFailure(rawJsonResourceInfo, patchResult)
                false
            }
            is SuccessServerSideApplyPatchResourceResult -> {
                deploymentListener.deploymentSuccess(rawJsonResourceInfo, patchResult)
                true
            }
        }
    }
}