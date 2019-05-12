package kuberig.example

import eu.rigeldev.kuberig.core.annotations.EnvFilter
import eu.rigeldev.kuberig.core.annotations.EnvResource
import eu.rigeldev.kuberig.core.annotations.Tick
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
    fun backendConfigInitial() : ConfigMapDsl {

        return configMap {

            metadata {
                name("backend-config")
            }

            data("environment.name", environment().name)
            data("app-config.properties", environmentFileText("files/custom-app-config.properties"))
            data("from.env.config", environmentConfig("something.environment.specific"))
            data("deploy.progress", "0%")
        }

    }

    @EnvResource
    @Tick(2)
    fun backendConfigComplete() : ConfigMapDsl {

        return configMap {

            metadata {
                name("backend-config")
            }

            data("environment.name", environment().name)
            data("app-config.properties", environmentFileText("files/custom-app-config.properties"))
            data("from.env.config", environmentConfig("something.environment.specific"))
            data("deploy.progress", "100%")
        }

    }

    @EnvResource
    @EnvFilter(environments = ["dev"])
    fun devConfig() : ConfigMapDsl {

        return configMap {

            metadata {
                name("dev-config")
            }

            data("some.dev.flags", "swagger")

        }
    }

    @EnvResource
    fun backendSecret(): SecretDsl {

        return secret {

            metadata {
                name("backend-secret")
            }

            // use an encrypted file
            data("app-secrets.properties", environmentFileBytes("files/.encrypted.custom-app-secrets.properties"))
            // use an encrypted config value
            data("some.password", environmentConfig("some.password").toByteArray())
        }

    }
}