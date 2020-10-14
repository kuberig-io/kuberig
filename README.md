[![KubeRig Logo](https://github.com/teyckmans/kuberig/blob/master/docs/images/website_logo_transparent_background.png)](https://kuberig.io)

![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/teyckmans/kuberig.svg?label=latest%20release)
[![teyckmans](https://circleci.com/gh/teyckmans/kuberig.svg?style=svg)](https://app.circleci.com/pipelines/github/teyckmans/kuberig)

# KubeRig

KubeRig helps you maintain Kubernetes/OpenShift resources in a smarter way. 
You define your resources with [Kotlin](https://kotlinlang.org/) code.

- Don't let template based tooling limit you. Use all the power of the [Kotlin](https://kotlinlang.org/) language to define your resources. 
- Alternative to [Helm](https://helm.sh/) for deploying resources.
- Alternative to [Helm](https://helm.sh/) Charts for sharing reusable packages.

## KubeRig Kotlin DSL
The KubeRig [Kotlin](https://kotlinlang.org/) DSL is generated based on the API specification of a Kubernetes or OpenShift cluster and provides a type-safe base to define your resources. 
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

### DSL availability
For both Kubernetes and OpenShift the DSLs are available on JCenter, for specific version availability please check their 
respective availability pages.
- [Kubernetes DSL availability](https://github.com/teyckmans/kuberig-dsl-kubernetes/blob/master/AVAILABILITY.MD)
- [OpenShift DSL availability](https://github.com/teyckmans/kuberig-dsl-openshift/blob/master/AVAILABILITY.MD)

## Additional Information
### User Manual
For more information please consult the [User Manual](https://teyckmans.github.io/kuberig/#/).

### Blog posts
You can find blog posts about KubeRig [here](https://rigel.dev/tag/kuberig/). 

### Site
[https://kuberig.io](https://kuberig.io).