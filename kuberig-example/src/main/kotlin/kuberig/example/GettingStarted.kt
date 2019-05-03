package kuberig.example

import eu.rigeldev.kuberig.core.annotations.ResourceGenerator
import kinds.v1.ConfigMapDsl
import kinds.v1.configMap
import java.io.File
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorContext.environment

class GettingStarted {

    @ResourceGenerator
    fun backendConfig() : ConfigMapDsl {

        return configMap {

            metadata {
                name("backend-config")
            }

            data("environment.name", environment().name)
            data("application.properties", File("application.properties").readText())
        }

    }
}