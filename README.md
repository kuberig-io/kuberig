# KubeRig

Helps you maintain Kubernetes/Openshift resources in a sane way. You define your resources with Kotlin code.

## Resource example:

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

It also demonstrates the use of the @EnvFilter annotation to mark this ConfigMap as only relevant for the 'dev' environment.

It also demonstrates the use of the @Tick annotation. The @Tick annotation allows control over when resources are applied during deployment.

## Gradle build example

The following Gradle build shows the use of the kuberig extension.

The extension allows the definition of multiple environments, what the target platform version is.

```kotlin
import java.time.Duration

plugins {
    id("eu.rigeldev.kuberig") version "0.0.10"
}

repositories {
    jcenter()
}

kuberig {
    kubernetes("v1.12.8")

    environments {
        create("local") {
            // tested with microk8s
            apiServer = "http://localhost:8080"
        }

        create("dev") {
            apiServer = "http://somewhere-else:8080"
        }
    }

    deployControl {
        tickRange = IntRange(1, 2)
        tickDuration = Duration.ofSeconds(10)
        tickGateKeeper = "eu.rigeldev.kuberig.core.deploy.control.DefaultTickGateKeeper"
    }
}
```

Give it a try, checkout the [kuberig-example](https://github.com/teyckmans/kuberig/tree/master/kuberig-example) directory.

## Authentication

Access tokens are stored encrypted in the environments/{environment-name}/.encrypted.{environment-name}.access-token file.
and decrypted for as short as possible when needed.

### Service Account setup

Create a service account and grant the edit role: 

```bash
kubectl create sa kuberig-deployer --namespace=default
kubectl create rolebinding kuberig-deployer-edit --clusterrole=edit --serviceaccount=default:kuberig-deployer --namespace=default
```

Retrieve the acces token:
```bash
kubectl describe sa kuberig-deployer --namespace=default
kubectl describe secret <name-of-token-secret>
```

Copy the token in environments/{environment-name}/.plain.{environment-name}.access-token

Run encrypt<Environment-name>Environment

You are ready to run deploy<Environment-name>Environment
