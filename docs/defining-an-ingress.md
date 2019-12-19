# Defining an Ingress

```kotlin
@EnvResource
fun simpleIngress(): IngressDsl {
    val appUrl = environmentConfig("app.url")
    val name = "simple-app"

    return ingress {
        metadata {
            name(name)
        }
        spec {
            rules {
                rule {
                    host(appUrl)
                    http {
                        paths {
                            path {
                                path("/")
                                backend {
                                    serviceName(name)
                                    servicePort(80)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```