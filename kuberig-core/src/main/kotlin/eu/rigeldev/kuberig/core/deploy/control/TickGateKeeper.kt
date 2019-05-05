package eu.rigeldev.kuberig.core.deploy.control

/**
 * A Tick gate keeper provides a hook into the Tick system.
 */
interface TickGateKeeper {

    /**
     * Before proceeding to the next tick, the Tick system will call this method in order to determine if it is ok
     * to proceed to the next tick.
     *
     * Should return true if it is ok to continue with the nextTick.
     *
     * Is also called for the first tick (currentTick will be zero, nextTick will be 1)
     */
    fun isGateOpen(currentTick: Int, nextTick: Int): Boolean
}