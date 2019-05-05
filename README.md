# kuberig

Helps you maintain Kubernetes/Openshift resources in a sane way. You define your resources with Kotlin code.

An example:

```kotlin
@EnvResource
@EnvFilter(environments = ["dev"])
@Tick(2)
fun backendConfigInitial() : ConfigMapDsl {

    return configMap {

        metadata {
            name("app-config")
        }

        data("app.parameter", "some-value")
    }

}
```

This example defines a simple ConfigMap resource by marking the method with @EnvResource.

It also demonstrates the use of the @EnvFilter annotation in to mark this ConfigMap as only relevant for the 'dev' environment.

It also demonstrates the use of the @Tick annotation. The @Tick annotation allows control over when resources are applied during deployment.