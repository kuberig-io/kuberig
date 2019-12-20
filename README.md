![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/teyckmans/kuberig.svg?label=latest%20release)

# KubeRig

KubeRig helps you maintain Kubernetes/OpenShift resources in a smarter way. 
You define your resources with [Kotlin](https://kotlinlang.org/) code.

## KubeRig DSL
The KubeRig DSL is generated based on the API specification of a Kubernetes or OpenShift cluster and provides a type-safe base to define your resources. 
This gives you auto-complete and automatic validation on version updates.

A quick example that defines a ConfigMap.
```kotlin
@EnvResource
fun basicConfigMap() : ConfigMapDsl {
    return configMap {
        metadata {
            name("app-config")
        }
        data("app.parameter", "some-value")
    }
}
```

## User Manual

For more information please consult the [User Manual](https://teyckmans.github.io/kuberig/#/).

## Blog posts

You can find blog posts about KubeRig [here](https://rigel.dev/tag/kuberig/). 
