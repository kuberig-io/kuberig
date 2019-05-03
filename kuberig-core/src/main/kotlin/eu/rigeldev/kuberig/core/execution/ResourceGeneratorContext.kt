package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.config.KubeRigEnvironment

object ResourceGeneratorContext {
    private val environment = ThreadLocal<KubeRigEnvironment>()

    fun environment() : KubeRigEnvironment {
        return environment.get()
    }

    fun fill(environment : KubeRigEnvironment) {
        ResourceGeneratorContext.environment.set(environment)
    }

    fun clear() {
        environment.remove()
    }
}