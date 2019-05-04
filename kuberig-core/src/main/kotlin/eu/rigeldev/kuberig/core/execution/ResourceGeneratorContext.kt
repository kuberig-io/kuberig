package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import java.util.*

object ResourceGeneratorContext {
    private val environment = ThreadLocal<KubeRigEnvironment>()
    private val environmentConfigs = ThreadLocal<Properties>()

    fun environment() : KubeRigEnvironment {
        return environment.get()
    }

    fun environmentConfig(configName: String) : String {
        return this.environmentConfigs.get().getProperty(configName) ?: throw IllegalStateException("Config $configName is not available for the ${environment.get().name} environment.")
    }

    fun environmentConfig(configName: String, defaultValue: String): String {
        return this.environmentConfigs.get().getProperty(configName, defaultValue)
    }


    fun fill(environment : KubeRigEnvironment,
             environmentConfigs : Properties) {
        this.environment.set(environment)
        this.environmentConfigs.set(environmentConfigs)
    }

    fun clear() {
        environment.remove()
        environmentConfigs.remove()
    }
}