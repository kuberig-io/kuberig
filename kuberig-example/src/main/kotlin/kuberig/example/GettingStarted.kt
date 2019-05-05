package kuberig.example

import eu.rigeldev.kuberig.core.annotations.EnvFilter
import eu.rigeldev.kuberig.core.annotations.EnvResource
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorContext.environment
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorContext.environmentConfig
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorContext.environmentFileBytes
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorContext.environmentFileText
import kinds.v1.ConfigMapDsl
import kinds.v1.SecretDsl
import kinds.v1.configMap
import kinds.v1.secret

class GettingStarted {

    @EnvResource
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

    @EnvResource
    @EnvFilter(environments = ["dev"])
    fun backendSecret(): SecretDsl {

        return secret {

            metadata {
                name("backend-secret")
            }

            data("app-secrets.properties", environmentFileBytes("files/custom-app-secrets.properties"))
        }

    }
}