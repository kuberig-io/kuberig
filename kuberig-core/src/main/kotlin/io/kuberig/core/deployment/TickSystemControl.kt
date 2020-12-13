package io.kuberig.core.deployment

import io.kuberig.core.deployment.control.DeployControl
import io.kuberig.core.deployment.control.TickGateKeeper

class TickSystemControl(
    deployControl: DeployControl,
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

    fun execute(newResourceInfoList: List<NewResourceInfo>, action: (NewResourceInfo) -> Boolean) {
        val newResourcesByTick = newResourceInfoList.groupBy { it.methodResult.tick }

        val tickIterator = tickRange.iterator()
        var currentTick = 0
        var gateOpen = true
        var forceStop = false

        while (!forceStop && tickIterator.hasNext()) {
            val nextTick = tickIterator.nextInt()

            gateOpen = tickGateKeeper.isGateOpen(currentTick, nextTick)
            if (gateOpen) {
                val tickResources = newResourcesByTick[nextTick] ?: listOf()

                deploymentListener.tickStart(nextTick, tickResources.size)

                val tickResourcesIterator = tickResources.iterator()

                while(!forceStop && tickResourcesIterator.hasNext()) {
                    val nextTickResource = tickResourcesIterator.next()

                    forceStop = !action(nextTickResource)
                }

                currentTick = nextTick

                if (!forceStop) {
                    deploymentListener.tickSuccess(nextTick)
                }
            }
        }

        if (!gateOpen) {
            deploymentListener.tickFailure(currentTick)
        }
    }
}