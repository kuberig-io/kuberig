package io.kuberig.core.deployment

import io.kuberig.core.resource.RawResourceInfo

class StatusTrackingDeploymentListener : DeploymentListener {

    var success = true
    var failureMessage = ""

    override fun tickStart(tickNumber: Int, amountOfResources: Int) {
        // not needed
    }

    override fun deploymentStart(rawResourceInfo: RawResourceInfo, resourceUrlInfo: ResourceUrlInfo) {
        // not needed
    }

    override fun deploymentSuccess(rawResourceInfo: RawResourceInfo, resourceResult: ResourceResult) {
        // not needed
    }

    override fun deploymentFailure(rawResourceInfo: RawResourceInfo, resourceResult: ResourceResult) {
        success = false
        failureMessage = "Failed to deploy ${rawResourceInfo.fullInfoText()}"
    }

    override fun tickSuccess(tickNumber: Int) {
        // not needed
    }

    override fun tickFailure(tickNumber: Int) {
        success = false
    }

}
