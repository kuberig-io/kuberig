package io.kuberig.core.deployment

import io.kuberig.core.resource.RawResourceInfo

interface DeploymentListener {

    fun tickStart(tickNumber: Int, amountOfResources: Int)

    fun deploymentStart(rawResourceInfo: RawResourceInfo, resourceUrlInfo: ResourceUrlInfo)

    fun deploymentSuccess(rawResourceInfo: RawResourceInfo, resourceResult: ResourceResult)

    fun deploymentFailure(rawResourceInfo: RawResourceInfo, resourceResult: ResourceResult)

    fun tickSuccess(tickNumber: Int)

    fun tickFailure(tickNumber: Int)

}