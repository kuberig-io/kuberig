package eu.rigeldev.kuberig.dsl.support

import eu.rigeldev.kuberig.dsl.DslType
import org.slf4j.LoggerFactory

/**
 * In order to publish multiple resources in a correct way it is up to the user of the kuberig-dsl to use
 * the emit function to publish them.
 *
 * Any other 'automatic' way would cause more problems than it would solve.
 */
object DslResourceEmitter {

    private val logger = LoggerFactory.getLogger(DslResourceReceiver::class.java)
    private val receivers = ThreadLocal<MutableList<DslResourceReceiver>>()

    fun init() {
        receivers.set(mutableListOf())
    }

    /**
     * The emit function can be used to publish one or more resources.
     */
    fun <T> emit(vararg dslTypes: DslType<T>) {
        receivers.get().forEach { receiver ->
            logger.info("[EMITTING] to ${receiver.getName()}")
            dslTypes.forEach {dslType ->
                try {
                    receiver.receive(dslType)
                } catch (e: Exception) {
                    logger.warn("Receiver ${receiver.getName()} failed to process dslType $dslType ")
                }
            }
        }
    }

    /**
     * Additional resource receivers can be registered.
     */
    fun registerReceiver(resourceReceiver: DslResourceReceiver) {
        this.receivers.get().add(resourceReceiver)
    }

    /**
     * Remove all registered resource receivers.
     */
    fun clearReceivers() {
        this.receivers.get().clear()
    }

    /**
     * Get a copy of the currently registered receivers.
     */
    fun receivers(): List<DslResourceReceiver> {
        return this.receivers.get().toList()
    }

}