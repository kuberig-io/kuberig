package io.kuberig.core.deployment

import io.kuberig.core.preparation.ResourceUrlInfo
import io.kuberig.core.resource.RawJsonResourceInfo

interface DeploymentListener {

    fun tickStart(tickNumber: Int, amountOfResources: Int)

    fun deploymentStart(rawJsonResourceInfo: RawJsonResourceInfo, resourceUrlInfo: ResourceUrlInfo)

    fun deploymentSuccess(rawJsonResourceInfo: RawJsonResourceInfo, resourceResult: ResourceResult)

    fun deploymentFailure(rawJsonResourceInfo: RawJsonResourceInfo, resourceResult: ResourceResult)

    fun tickSuccess(tickNumber: Int)

    fun tickFailure(tickNumber: Int)

}