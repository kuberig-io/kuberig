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
                val amountOfTasks = nextTick.crdDefinitionDeploymentTasks.size + nextTick.otherDeploymentTasks.size

                deploymentListener.tickStart(nextTick.tickNumber, amountOfTasks)

                forceStop = executeTasks(nextTick.crdDefinitionDeploymentTasks, action)
                if (!forceStop) {
                    forceStop = executeTasks(nextTick.otherDeploymentTasks, action)
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

    private fun executeTasks(tasks: List<DeploymentTask>, action: (DeploymentTask) -> Boolean): Boolean {
        var forceStop = false

        if (tasks.isNotEmpty()) {
            val tasksIterator = tasks.iterator()

            while (!forceStop && tasksIterator.hasNext()) {
                val nextTask = tasksIterator.next()

                forceStop = !action(nextTask)
            }
        }

        return forceStop
    }
}