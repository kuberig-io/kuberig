package io.kuberig.core.deployment

import java.lang.Exception

object DeploymentListenerRegistry : DeploymentListener {

    private val listeners = mutableListOf<DeploymentListener>()

    fun registerListener(listener: DeploymentListener) {
        listeners.add(listener)
    }

    override fun tickStart(tickNumber: Int, amountOfResources: Int) {
        notifyListeners {
            tickStart(tickNumber, amountOfResources)
        }
    }

    override fun deploymentStart(newResourceInfo: NewResourceInfo, resourceUrlInfo: ResourceUrlInfo) {
        notifyListeners {
            deploymentStart(newResourceInfo, resourceUrlInfo)
        }
    }

    override fun deploymentSuccess(newResourceInfo: NewResourceInfo, resourceResult: ResourceResult) {
        notifyListeners {
            deploymentSuccess(newResourceInfo, resourceResult)
        }
    }

    override fun deploymentFailure(newResourceInfo: NewResourceInfo, resourceResult: ResourceResult) {
        notifyListeners {
            deploymentFailure(newResourceInfo, resourceResult)
        }
    }

    override fun tickSuccess(tickNumber: Int) {
        notifyListeners {
            tickSuccess(tickNumber)
        }
    }

    override fun tickFailure(tickNumber: Int) {
        notifyListeners {
            tickFailure(tickNumber)
        }
    }

    private fun notifyListeners(action: DeploymentListener.() -> Unit) {
        for (listener in listeners) {
            try {
                listener.action()
            } catch (e: Exception) {
                e.printStackTrace() // TODO wire in logger
            }
        }
    }

    fun reset() {
        this.listeners.clear()
    }
}