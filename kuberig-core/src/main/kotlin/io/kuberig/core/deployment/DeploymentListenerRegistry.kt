package io.kuberig.core.deployment

import io.kuberig.core.resource.RawResourceInfo
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

    override fun deploymentStart(rawResourceInfo: RawResourceInfo, resourceUrlInfo: ResourceUrlInfo) {
        notifyListeners {
            deploymentStart(rawResourceInfo, resourceUrlInfo)
        }
    }

    override fun deploymentSuccess(rawResourceInfo: RawResourceInfo, resourceResult: ResourceResult) {
        notifyListeners {
            deploymentSuccess(rawResourceInfo, resourceResult)
        }
    }

    override fun deploymentFailure(rawResourceInfo: RawResourceInfo, resourceResult: ResourceResult) {
        notifyListeners {
            deploymentFailure(rawResourceInfo, resourceResult)
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