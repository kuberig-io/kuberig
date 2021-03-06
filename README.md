[![KubeRig Logo](https://kuberig.io/img/logo/website_logo_transparent_background.png)](https://kuberig.io)

![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/kuberig-io/kuberig.svg?label=latest%20release)
[![kuberig-io](https://circleci.com/gh/kuberig-io/kuberig.svg?style=svg)](https://app.circleci.com/pipelines/github/kuberig-io/kuberig)

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
- [Kubernetes DSL availability](https://github.com/kuberig-io/kuberig-dsl-kubernetes/blob/master/AVAILABILITY.MD)
- [OpenShift DSL availability](https://github.com/kuberig-io/kuberig-dsl-openshift/blob/master/AVAILABILITY.MD)

Our jobs detect and add new Kubernetes versions automatically. We get a Slack notification to create the JCenter inclusion request for the new version(s). 
These requests usually get processed within the next day.

The OpenShift job is not working. The API specification is no longer available in the repositories. 
This means we need to startup an OpenShift cluster to get the OpenAPI specification. I don't have the time and funding to do this. 

> If you have access to specific versions please provide the OpenAPI specification, then I can add it. Or even better open a pull request with it.

## Additional Information
### User Manual
For more information please consult the [User Manual](https://kuberig.io/docs/home.html).

### Blog posts
You can find blog posts about KubeRig [here](https://kuberig.io/blog/). 

### Site
[https://kuberig.io](https://kuberig.io).