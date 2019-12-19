# Defining a ConfigMap

```kotlin
@EnvResource
fun basicConfigMap() : ConfigMapDsl {
    return configMap {
        metadata {
            name("basic-configmap")
        }
        // use a plain configuration parameter (environment specific)
        data("app.parameter", environmentConfig("some.parameter"))
        // include a plain text file (environment specific)
        data("app-config.properties", environmentFileText("files/custom-app-config.properties"))
    }
}
```