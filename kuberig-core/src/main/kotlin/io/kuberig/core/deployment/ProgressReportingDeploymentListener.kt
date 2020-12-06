package io.kuberig.core.deployment

class ProgressReportingDeploymentListener(private val applyStrategy: ApplyStrategy<Any>) : DeploymentListener {
    override fun tickStart(tickNumber: Int, amountOfResources: Int) {
        println("[TICK-SYSTEM][TICK#$tickNumber] processing $amountOfResources resource(s).")
    }

    override fun deploymentStart(newResourceInfo: NewResourceInfo, resourceUrlInfo: ResourceUrlInfo) {
        println("------")
        println("deploying ${newResourceInfo.infoText()}...")
    }

    override fun deploymentSuccess(newResourceInfo: NewResourceInfo, resourceResult: ResourceResult) {
        resourceResult.logInfo(applyStrategy)
    }

    override fun deploymentFailure(newResourceInfo: NewResourceInfo, resourceResult: ResourceResult) {
        resourceResult.logInfo(applyStrategy)
    }

    override fun tickSuccess(tickNumber: Int) {
        println("[TICK-SYSTEM][TICK#$tickNumber] processed.")
    }

    override fun tickFailure(tickNumber: Int) {
        println("[TICK-SYSTEM] ERROR - gate keeper closed gate at tick $tickNumber!")
    }
}