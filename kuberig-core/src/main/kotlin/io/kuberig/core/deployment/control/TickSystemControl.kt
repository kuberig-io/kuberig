package io.kuberig.core.deployment.control

import io.kuberig.core.deployment.DeploymentListener
import io.kuberig.core.deployment.DeploymentPlan
import io.kuberig.core.deployment.DeploymentTask

class TickSystemControl(
    deployControl: TickInfo,
    resourceGenerationRuntimeClasspathClassLoader: ClassLoader,
    private val deploymentListener: DeploymentListener
) {

    private val tickRange = IntRange(deployControl.tickRangeStart, deployControl.tickRangeEnd)
    private val tickGateKeeper: TickGateKeeper

    init {
        check(!tickRange.isEmpty()) { "No ticks to process." }

        @Suppress("UNCHECKED_CAST")
        val tickGateKeeperType: Class<out TickGateKeeper> = resourceGenerationRuntimeClasspathClassLoader
            .loadClass(deployControl.tickGateKeeper) as Class<out TickGateKeeper>

        tickGateKeeper = tickGateKeeperType.getConstructor().newInstance()
    }

    fun execute(deploymentPlan: DeploymentPlan, action: (DeploymentTask) -> Boolean) {
        val tickIterator = deploymentPlan.ticks.iterator()
        var currentTick = 0
        var gateOpen = true
        var forceStop = false

        while (!forceStop && tickIterator.hasNext()) {
            val nextTick = tickIterator.next()

            gateOpen = tickGateKeeper.isGateOpen(currentTick, nextTick.tickNumber)
            if (gateOpen) {
                val tickDeploymentTasks = nextTick.deploymentTasks

                deploymentListener.tickStart(nextTick.tickNumber, tickDeploymentTasks.size)

                val tickDeploymentTasksIterator = tickDeploymentTasks.iterator()

                while(!forceStop && tickDeploymentTasksIterator.hasNext()) {
                    val nextTickDeploymentTask = tickDeploymentTasksIterator.next()

                    forceStop = !action(nextTickDeploymentTask)
                }

                currentTick = nextTick.tickNumber

                if (!forceStop) {
                    deploymentListener.tickSuccess(nextTick.tickNumber)
                }
            }
        }

        if (!gateOpen) {
            deploymentListener.tickFailure(currentTick)
        }
    }
}