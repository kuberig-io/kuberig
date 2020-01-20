# Quick DSL intro

In the [Quick Start](quick-start.md) we deployed some resources but did not go into the details of to use the KubeRig DSL to define them.

Using the KubeRig DSL it is possible to generate Kubernetes resources in a smart and typesafe way. Lets dig into it!

## Step 1
The example resources are based on the [Using Ingress](https://kind.sigs.k8s.io/docs/user/ingress/) section of the Kind docs.

As a first step we have converted the yaml files to DSL code one-on-one. But we can do a better job now that it is code.

```kotlin
package ingress

import eu.rigeldev.kuberig.core.annotations.EnvResource
import kinds.extensions.v1beta1.IngressDsl
import kinds.extensions.v1beta1.ingress
import kinds.v1.PodDsl
import kinds.v1.ServiceDsl
import kinds.v1.pod
import kinds.v1.service

class IngressExample {

    @EnvResource
    fun fooApp(): PodDsl {
        return pod {
            metadata {
                name("foo-app")
                labels {
                    label("app", "foo")
                }
            }
            spec {
                containers {
                    container {
                        name("foo-app")
                        image("hashicorp/http-echo")
                        args {
                            arg("-text=foo")
                        }
                    }
                }
            }
        }
    }

    @EnvResource
    fun fooService(): ServiceDsl {
        return service {
            metadata {
                name("foo-service")
            }
            spec {
                selector("app", "foo")
                ports {
                    port {
                        // Default port used by the image
                        port(5678)
                    }
                }
            }
        }
    }

    @EnvResource
    fun barApp(): PodDsl {
        return pod {
            metadata {
                name("bar-app")
                labels {
                    label("app", "bar")
                }
            }
            spec {
                containers {
                    container {
                        name("bar-app")
                        image("hashicorp/http-echo")
                        args {
                            arg("-text=bar")
                        }
                    }
                }
            }
        }
    }

    @EnvResource
    fun barService(): ServiceDsl {
        return service {
            metadata {
                name("bar-service")
            }
            spec {
                selector("app", "bar")
                ports {
                    port {
                        // Default port used by the image
                        port(5678)
                    }
                }
            }
        }
    }

    @EnvResource
    fun exampleIngress(): IngressDsl {
        return ingress {
            metadata {
                name("example-ingress")
                annotations {
                    annotation("ingress.kubernetes.io/rewrite-target", "/")
                }
            }
            spec {
                rules {
                    rule {
                        http {
                            paths {
                                path {
                                    path("/foo")
                                    backend {
                                        serviceName("foo-service")
                                        servicePort(5678)
                                    }
                                }
                                path {
                                    path("/bar")
                                    backend {
                                        serviceName("bar-service")
                                        servicePort(5678)
                                    }
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

In this example we use the `@EnvResource` annotation. Methods annotated with `@EnvResource` are expected to return a DSL type that exist in the `kinds` package.

## Step 2

Both the foo and bar pod and service are similar so we can extract this in a dedicated class.
```kotlin
package ingress

import eu.rigeldev.kuberig.core.annotations.EnvResource
import kinds.v1.PodDsl
import kinds.v1.ServiceDsl
import kinds.v1.pod
import kinds.v1.service

abstract class HttpEchoApp(private val name: String) {

    @EnvResource
    fun appPod(): PodDsl {
        return pod {
            metadata {
                name("$name-app")
                labels {
                    label("app", name)
                }
            }
            spec {
                containers {
                    container {
                        name("$name-app")
                        image("hashicorp/http-echo")
                        args {
                            arg("-text=$name")
                        }
                    }
                }
            }
        }
    }

    @EnvResource
    fun appService(): ServiceDsl {
        return service {
            metadata {
                name("$name-service")
            }
            spec {
                selector("app", name)
                ports {
                    port {
                        // Default port used by the image
                        port(5678)
                    }
                }
            }
        }
    }
}

class FooApp : HttpEchoApp("foo")
class BarApp : HttpEchoApp("bar")
```

And we use this class to create FooApp and BarApp specific classes. This shows that you can place the `@EnvResource` annotation on methods of parent classes.

The `@EnvResource` annotation is great to start with but it is limited because it can only be used to define a single resource.

## Level Up Please!

For more complex scenarios we can use the `@EnvResources` annotation. 
When a method is annotated with the `@EnvResources` annotation we can use the `emit` function of the `DslResourceEmitter` to define multiple resources.

Lets take our example to the next level.

Our top level IngressExample class should not be too difficult. We should be able to just specify some configuration of what we want.
```kotlin
package ingress

import eu.rigeldev.kuberig.core.annotations.EnvResources

class IngressExample {

    @EnvResources
    fun exampleIngressResources() {
        HttpEchoApps.echoApps("foo", "bar")
    }

}
```

The new HttpEchoApps object exposes an echoApps method that takes in some configuration, in this case only names are needed.
```kotlin
package ingress

import eu.rigeldev.kuberig.dsl.support.DslResourceEmitter.emit
import kinds.extensions.v1beta1.IngressDsl
import kinds.extensions.v1beta1.ingress

object HttpEchoApps {

    fun echoApps(vararg appNames: String) {
        val apps = appNames.map {
            appName -> HttpEchoApp(appName)
        }

        apps.forEach { app ->
            app.emitResources()
        }

        emit(exampleIngress(apps))
    }

    private fun exampleIngress(apps: List<HttpEchoApp>): IngressDsl {
        return ingress {
            metadata {
                name("example-ingress")
                annotations {
                    annotation("ingress.kubernetes.io/rewrite-target", "/")
                }
            }
            spec {
                rules {
                    rule {
                        http {
                            paths {
                                apps.forEach { app ->
                                    path(app.appIngressPath())
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
THe HttpEchoApps object uses the names to create an instance of HttpEchoApp for every name that comes in.

Next it ask each HttpEchoApp to `emit` the resources needed to deploy an HttpEchoApp.

And as a last action the ingress resource is created by using the list of HttpEchoApp instances.
In the `paths` block we ask each HttpEchoApp for the ingressPath and add it to the ingress.

The HttpEchoApp looks pretty similar as before. 
 
```kotlin
package ingress

import eu.rigeldev.kuberig.dsl.support.DslResourceEmitter.emit
import io.k8s.api.extensions.v1beta1.HTTPIngressPathDsl
import io.k8s.api.extensions.v1beta1.hTTPIngressPath
import kinds.v1.PodDsl
import kinds.v1.ServiceDsl
import kinds.v1.pod
import kinds.v1.service

class HttpEchoApp(private val name: String) {
    // Default port used by the image
    private val portNumber = 5678
    
    fun emitResources() {
        emit(appPod())
        emit(appService())
    }
    
    private fun appPod(): PodDsl {
        return pod {
            metadata {
                name("$name-app")
                labels {
                    label("app", name)
                }
            }
            spec {
                containers {
                    container {
                        name("$name-app")
                        image("hashicorp/http-echo")
                        args {
                            arg("-text=$name")
                        }
                    }
                }
            }
        }
    }

    private fun appService(): ServiceDsl {
        return service {
            metadata {
                name("$name-service")
            }
            spec {
                selector("app", name)
                ports {
                    port {
                        port(portNumber)
                    }
                }
            }
        }
    }

    fun appIngressPath(): HTTPIngressPathDsl {
        return hTTPIngressPath {
            path("/$name")
            backend {
                serviceName("$name-service")
                servicePort(portNumber)
            }
        }
    }
}
```
The `@EnvResource` annotations have been removed. The pod and service definition methods have become private.
The appIngressPath method is new and generates a part of the ingress resource that is used in the HttpEchoApps.exampleIngress method.
Everything that is specific to an HttpEchoApp is now defined inside the HttpEchoApp class.

## Next steps
I hope this example shows the potential of the KubeRig DSL and has sparkelled your enthusiasm!

There is much more to KubeRig so read on! 


 
