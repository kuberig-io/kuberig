# KubeRig

Helps you maintain Kubernetes/Openshift resources in a sane way. You define your resources with Kotlin code.

## Resource example:

```kotlin
@EnvResource
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

## Gradle build example

The following Gradle build shows the use of the kuberig extension.

The extension allows the definition of multiple environments, what the target platform version is.

```kotlin
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
}
```

For each environment the following actions are available. Task names are {action}{Environment-name}Environment.

|action|description|
|----|-----------|
|createEncryptionKey|Generates an encryption key for the environment|
|encrypt|Will encrypt all files that have a filename prefixed with .plain. and create/overwrite the .encrypted. file for all files in the environments/{environment-name} directory.|
|encryptFile|Will encrypt the file specified via the --file option|
|encryptValue|Will encrypt the value specified via the --value option, output can be used in the environments/{environment-name}/{environment-name}-configs.properties file|
|decrypt|Will decrypt all files that have a filename prefixed with .encrypted. and create the .plain. decrypted file for all files in the environments/{environment-name} directory.|
|decryptFile|Will decrypt the file specified via the --file option|
|decryptValue|Will decrypt the value specified via the --value option, value should be the complete output of encryptValue|
|deploy|Will create/update all resources applicable for the environment|
|generateYaml|Will generate yaml files for all resources applicable for the environment in the build/generated-yaml/{environment-name} directory|

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

Run encrypt{Environment-name}Environment

You are ready to run deploy{Environment-name}Environment
