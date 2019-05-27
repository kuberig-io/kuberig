# KubeRig

KubeRig helps you maintain Kubernetes/Openshift resources in a sane way. You define your resources with Kotlin code.

The Kotlin DSL is generated based on the api specification of a cluster. For vanilla Kubernetes and Openshift you can find the supported versions here:
- [Kubernetes](https://github.com/teyckmans/kuberig-dsl-kubernetes)
- [Openshift](https://github.com/teyckmans/kuberig-dsl-openshift)

In case you have CRDs you can use the KubeRig DSL generator [here](https://github.com/teyckmans/kuberig-dsl). 

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
    id("eu.rigeldev.kuberig") version "0.0.15"
}

repositories {
    jcenter()
}
```



For each environment the following actions are available. Task names are {action}{Environment-name}Environment.

|action|description|
|----|-----------|
|createEncryptionKey|Generates an encryption key for the environment|
|encrypt|Will encrypt all files that have a filename prefixed with .plain. and create/overwrite the .encrypted. file for all files in the environments/{environment-name} directory.|
|encryptFile|Will encrypt the file specified via the --file option|
|encryptConfig|Will encrypt the value of the property in the environment config properties file specified via the --key option|
|decrypt|Will decrypt all files that have a filename prefixed with .encrypted. and create the .plain. decrypted file for all files in the environments/{environment-name} directory.|
|decryptFile|Will decrypt the file specified via the --file option|
|decryptConfig|Will decrypt the value of the property in the environment config properties file specified via the --key option|
|deploy|Will create/update all resources applicable for the environment|
|generateYaml|Will generate yaml files for all resources applicable for the environment in the build/generated-yaml/{environment-name} directory|

Give it a try, checkout the [kuberig-example](https://github.com/teyckmans/kuberig/tree/master/kuberig-example) directory.

## Encryption support/setup

KubeRig uses the [Google Tink](https://github.com/google/tink) library in order to provide encryption support. 

If you prefix all sensitive filenames with `.plain.` and configure your .gitignore file properly by using the `initGitIgnore` task
you can prevent yourself from committing sensitive information. But even then it requires a lot of rigor to not accidentally commit sensitive information. You have been warned!

So before proceeding please run the `initGitIgnore` task and remember to prefix files that contain sensitive information with the `.plain.` prefix! 

You create an encryption key for every environment by using the `createEncryptionKey{Environment-name}Environment` task. We currently generate a AeadKeyTemplates.AES256_CTR_HMAC_SHA256 keyset.

This will create a {environment-name}.keyset file in the `environment` directory. You should **NEVER EVER** commit the {environment-name}.keyset file. If you have used the `initGitIgnore` task this file will already get ignored.

You should have multiple secure backups of the .keyset files without them you can't decrypt/deploy to the environment. If you loose a .keyset file we can't help you.

When you run the `encrypt{Environment-name}Environment` task all files that have the `.plain.` prefix will be encrypted. A file prefixed with `.encrypted.` will be created and the `.plain.` file will be removed.

The `decrypt{Environment-name}Environment` works in the other direction. The only difference is that it will not delete the files with the `.encrypted.` prefix. 

## Repository Organization

A KubeRig repository follows a well defined directory structure.

The root directory is referred to as the project directory. 
- It contains the [Gradle wrapper files](https://docs.gradle.org/current/userguide/gradle_wrapper.html). 
- It contains the `environments` directory.
- It contains the `src\main\kotlin` directory.
- It contains the `.gitignore` file. You can initialize it by executing the `initGitIgnore` task.
- It contains the `settings.gradle.kts` and `build.gradle.kts` Gradle build files.

The `environments` directory contains a sub directory for every environment defined in the build file.

Each `environment` directory contains the following files:
- {environment-name}.keyset the encryption key of the environment. You should **NEVER EVER** commit the {environment-name}.keyset file. If you have used the `initGitIgnore` task this file will already get ignored.
- .encrypted.{environment-name}.access-token the encrypted version of the JWT access token of service account that can be used to do deployments.
- {environment-name}-config.properties a properties file with environment specific configuration parameters. Can contain encrypted values use the `encryptValue{Environment-name}Environment` task to encrypt values.
- other files that are environment specific, remember to prefix files that contains sensitive information with `.plain.` . 

The `src\main\kotlin` directory is where you place your [Kotlin](https://kotlinlang.org/) resource generation code.

The kuberig extension in the `build.gradle.kts` file allows you to define your environments, target platform and more. 

## Authentication

Access tokens are stored encrypted in the environments/{environment-name}/.encrypted.{environment-name}.access-token file.

### Service Account setup

Create a service account and grant the edit role: 

```bash
kubectl create sa kuberig-deployer --namespace=default
kubectl create rolebinding kuberig-deployer-edit --clusterrole=edit --serviceaccount=default:kuberig-deployer --namespace=default
```

Retrieve the access token:
```bash
kubectl describe sa kuberig-deployer --namespace=default
kubectl describe secret <name-of-token-secret>
```

Copy the token in environments/{environment-name}/.plain.{environment-name}.access-token

Run createEncryptionKey{Environment-name}Environment if you have not done so already. 

Run encrypt{Environment-name}Environment.

You are ready to run deploy{Environment-name}Environment

## More information
You can find more information on various aspects and use cases about KubeRig [here](https://rigel.dev/tag/kuberig/). 
