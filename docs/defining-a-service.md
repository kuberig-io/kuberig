# Defining a Service

```kotlin
@EnvResource
fun basicService(): ServiceDsl {
    // label can be reused in the deployment definition.
    val label = Pair("app", "my-app")

    return service {
        metadata {
            name("basic-service")
        }
        spec {
            selector(label)
            ports {
                port {
                    port(80)
                    targetPort(8080)
                }
            }
        }
    }
}
```