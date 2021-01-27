package io.kuberig.core.deployment

import io.kuberig.core.preparation.ResourceUrlInfo
import io.kuberig.core.resource.RawJsonResourceInfo
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

    override fun deploymentStart(rawJsonResourceInfo: RawJsonResourceInfo, resourceUrlInfo: ResourceUrlInfo) {
        notifyListeners {
            deploymentStart(rawJsonResourceInfo, resourceUrlInfo)
        }
    }

    override fun deploymentSuccess(rawJsonResourceInfo: RawJsonResourceInfo, resourceResult: ResourceResult) {
        notifyListeners {
            deploymentSuccess(rawJsonResourceInfo, resourceResult)
        }
    }

    override fun deploymentFailure(rawJsonResourceInfo: RawJsonResourceInfo, resourceResult: ResourceResult) {
        notifyListeners {
            deploymentFailure(rawJsonResourceInfo, resourceResult)
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