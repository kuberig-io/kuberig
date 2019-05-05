package kuberig.example

import eu.rigeldev.kuberig.core.annotations.ResourceGenerator
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorContext.environment
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorContext.environmentConfig
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorContext.environmentFileText
import kinds.v1.ConfigMapDsl
import kinds.v1.configMap

class GettingStarted {

    @ResourceGenerator
    fun backendConfig() : ConfigMapDsl {

        return configMap {

            metadata {
                name("backend-config")
            }

            data("environment.name", environment().name)
            data("app-config.properties", environmentFileText("files/custom-app-config.properties"))
            data("from.env.config", environmentConfig("something.environment.specific"))
        }

    }
}