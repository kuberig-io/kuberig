package eu.rigeldev.kuberig.core.deploy.control

/**
 * The default tick gate keeper always allows proceeding to the next tick.
 */
class DefaultTickGateKeeper : TickGateKeeper {

    override fun isGateOpen(currentTick: Int, nextTick: Int): Boolean {
        return true
    }
}