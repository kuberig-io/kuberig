# Defining a Deployment

```kotlin
private val name = "simple-app"
private val label = Pair("app", name)

@EnvResource
fun appDeployment(): DeploymentDsl {
    return deployment {
        metadata {
            name(name)
        }
        spec {
            replicas(2)
            selector {
                matchLabels {
                    matchLabel(label)
                }
            }
            template {
                metadata {
                    labels {
                        label(label)
                    }
                }
                spec {
                    containers {
                        container {
                            name(name)
                            image("simple-app:${containerVersion("simple-app")}")
                        }
                    }
                }
            }
        }
    }
}
```

The `containerVersion` helper function will retrieve the actual image version/tag to use from a `container.versions` file.
First the environment specific `container.versions` file will be checked for an entry for 'simple-app'.
If no entry is available the global `container.versions` file will be used instead.