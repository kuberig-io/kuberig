package io.kuberig.core.deployment

import io.kuberig.core.preparation.ResourceUrlInfo
import io.kuberig.core.resource.RawJsonResourceInfo

class ProgressReportingDeploymentListener(private val applyStrategy: ApplyStrategy<Any>) : DeploymentListener {
    override fun tickStart(tickNumber: Int, amountOfResources: Int) {
        println("[TICK-SYSTEM][TICK#$tickNumber] processing $amountOfResources resource(s).")
    }

    override fun deploymentStart(rawJsonResourceInfo: RawJsonResourceInfo, resourceUrlInfo: ResourceUrlInfo) {
        println("------")
        println("deploying ${rawJsonResourceInfo.infoText()}...")
    }

    override fun deploymentSuccess(rawJsonResourceInfo: RawJsonResourceInfo, resourceResult: ResourceResult) {
        resourceResult.logInfo(applyStrategy)
    }

    override fun deploymentFailure(rawJsonResourceInfo: RawJsonResourceInfo, resourceResult: ResourceResult) {
        resourceResult.logInfo(applyStrategy)
    }

    override fun tickSuccess(tickNumber: Int) {
        println("[TICK-SYSTEM][TICK#$tickNumber] processed.")
    }

    override fun tickFailure(tickNumber: Int) {
        println("[TICK-SYSTEM] ERROR - gate keeper closed gate at tick $tickNumber!")
    }
}