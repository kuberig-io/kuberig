package io.kuberig.core.deployment

import io.kuberig.core.resource.RawResourceInfo

class ProgressReportingDeploymentListener(private val applyStrategy: ApplyStrategy<Any>) : DeploymentListener {
    override fun tickStart(tickNumber: Int, amountOfResources: Int) {
        println("[TICK-SYSTEM][TICK#$tickNumber] processing $amountOfResources resource(s).")
    }

    override fun deploymentStart(rawResourceInfo: RawResourceInfo, resourceUrlInfo: ResourceUrlInfo) {
        println("------")
        println("deploying ${rawResourceInfo.infoText()}...")
    }

    override fun deploymentSuccess(rawResourceInfo: RawResourceInfo, resourceResult: ResourceResult) {
        resourceResult.logInfo(applyStrategy)
    }

    override fun deploymentFailure(rawResourceInfo: RawResourceInfo, resourceResult: ResourceResult) {
        resourceResult.logInfo(applyStrategy)
    }

    override fun tickSuccess(tickNumber: Int) {
        println("[TICK-SYSTEM][TICK#$tickNumber] processed.")
    }

    override fun tickFailure(tickNumber: Int) {
        println("[TICK-SYSTEM] ERROR - gate keeper closed gate at tick $tickNumber!")
    }
}