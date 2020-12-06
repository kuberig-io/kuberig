package io.kuberig.core.deployment

class StatusTrackingDeploymentListener : DeploymentListener {

    var success = true
    var failureMessage = ""

    override fun tickStart(tickNumber: Int, amountOfResources: Int) {
        // not needed
    }

    override fun deploymentStart(newResourceInfo: NewResourceInfo, resourceUrlInfo: ResourceUrlInfo) {
        // not needed
    }

    override fun deploymentSuccess(newResourceInfo: NewResourceInfo, resourceResult: ResourceResult) {
        // not needed
    }

    override fun deploymentFailure(newResourceInfo: NewResourceInfo, resourceResult: ResourceResult) {
        success = false
        failureMessage = "Failed to deploy ${newResourceInfo.fullInfoText()}"
    }

    override fun tickSuccess(tickNumber: Int) {
        // not needed
    }

    override fun tickFailure(tickNumber: Int) {
        success = false
    }

}
