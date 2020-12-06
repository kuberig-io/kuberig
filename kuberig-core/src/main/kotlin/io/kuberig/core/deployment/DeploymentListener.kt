package io.kuberig.core.deployment

interface DeploymentListener {

    fun tickStart(tickNumber: Int, amountOfResources: Int)

    fun deploymentStart(newResourceInfo: NewResourceInfo, resourceUrlInfo: ResourceUrlInfo)

    fun deploymentSuccess(newResourceInfo: NewResourceInfo, resourceResult: ResourceResult)

    fun deploymentFailure(newResourceInfo: NewResourceInfo, resourceResult: ResourceResult)

    fun tickSuccess(tickNumber: Int)

    fun tickFailure(tickNumber: Int)

}