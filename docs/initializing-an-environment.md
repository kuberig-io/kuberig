# Initializing an Environment

## Details
The `initEnvironment` task is intended to make it easy to configure a KubeRig environment and start deploying resources to an existing Kubernetes Cluster.

The `initEnvironment` task will make sure that:
- The environment directory (environments/$environment.name$) is created 
- The environment configs file ($environment.name$-configs.properties) is created inside the environment directory.
- The api server url is added to the environment configs file as an encrypted value.
- The environment encryption key ($environment-name$.keyset.json) is generated inside the environment directory.

Everything except for the environment encryption key are safe to commit.
I advise you to create a secure backup of your environment encryption key.

## Flags
### `--name`
The name to use for the new environment.

### `--apiServerUrl`
Mostly only usable for local development environments without authentication/authorization like [microk8s](https://microk8s.io/).

No service account is created when using this flag. Check [here](service-account-setup.md) for information on how to setup a service account for KubeRig.

### `--currentKubectlContext`
When using the `--currentKubectlContext` flag there is no need to specify the `--apiServerUrl` flag because all details will be read from the `kubectl` config file.

The `--currentKubectlContext` flag triggers the initEnvironment to use the current Kubectl context to create the kuberig service account. 

The kuberig service account is created in the namespace of the Kubectl context. In case no namespace is set the default namespace is used.

A kuberig-edit rolebinding is created granting ClusterRole edit to the kuberig service account.

The access token for the kuberig service account is added to the environment and encrypted.

## Limitations
The Kubectl configuration file has a lot of possible ways to configure access to a Kubernetes cluster and it will take more work to make the `--currentKubectlContext` deal with them all. 

Currently it is known to work for:
- Digital Ocean Kubernetes service
- Google Kubernetes Engine
- Amazone Elastic Kubernetes Service
- minikube
- KIND
- microk8s

In case you run into problems please create an issue on [github](https://github.com/kuberig-io/kuberig/issues) or jump in and create a pull-request.

## In Action
- [Digital Ocean Kubernetes (DOKS)](https://www.rigel.dev/kuberig-doks/)
- [Google Kubernetes Engine (GKE)](https://www.rigel.dev/kuberig-gke/)
- [Elastic Kubernetes Service (Amazone EKS)](https://www.rigel.dev/kuberig-eks/)
- [KubeRig + microk8s](https://www.rigel.dev/kuberig-microk8s/)

