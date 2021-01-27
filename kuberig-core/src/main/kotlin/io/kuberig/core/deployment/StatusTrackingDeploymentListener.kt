package io.kuberig.core.deployment

import io.kuberig.core.preparation.ResourceUrlInfo
import io.kuberig.core.resource.RawJsonResourceInfo

class StatusTrackingDeploymentListener : DeploymentListener {

    var success = true
    var failureMessage = ""

    override fun tickStart(tickNumber: Int, amountOfResources: Int) {
        // not needed
    }

    override fun deploymentStart(rawJsonResourceInfo: RawJsonResourceInfo, resourceUrlInfo: ResourceUrlInfo) {
        // not needed
    }

    override fun deploymentSuccess(rawJsonResourceInfo: RawJsonResourceInfo, resourceResult: ResourceResult) {
        // not needed
    }

    override fun deploymentFailure(rawJsonResourceInfo: RawJsonResourceInfo, resourceResult: ResourceResult) {
        success = false
        failureMessage = "Failed to deploy ${rawJsonResourceInfo.fullInfoText()}"
    }

    override fun tickSuccess(tickNumber: Int) {
        // not needed
    }

    override fun tickFailure(tickNumber: Int) {
        success = false
    }

}
