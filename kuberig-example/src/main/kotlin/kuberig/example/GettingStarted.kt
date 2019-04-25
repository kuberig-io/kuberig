package kuberig.example

import eu.rigeldev.kuberig.core.annotations.KubeRigResource
import kinds.v1.ConfigMapDsl
import kinds.v1.configMap
import java.io.File

class GettingStarted {

    @KubeRigResource
    fun backendConfig() : ConfigMapDsl {

        return configMap {

            metadata {
                name("backend-config")
            }

            data("application.properties", File("application.properties").readText())
        }

    }
}