# Quick Start

The fastest way to get started is by cloning or downloading the [kuberig-starter](https://github.com/teyckmans/kuberig-starter) repository.

## Cloning
Clone the git repository.  
```shell
$ git clone https://github.com/teyckmans/kuberig-starter
```

## Downloading
Download the [kuberig-starter zip](https://github.com/teyckmans/kuberig-starter/archive/master.zip) and extract it.

## Prerequisites
As KubeRig is a Gradle plugin, it needs a [Java JDK or JRE](http://www.oracle.com/technetwork/java/javase/downloads/index.html) version 8 or higher to be installed. To check, run `java -version`:
```shell
$ java -version 
java version "1.8.0_121"
```

## Check
Open a terminal in the git clone directory or extracted zip directory and check that the KubeRig tasks are available.

```shell
$ cd kuberig-starter
$ ./gradlew tasks --group kuberig

...

> Task :tasks

------------------------------------------------------------
Tasks runnable from root project
------------------------------------------------------------

Kuberig tasks
-------------
clearContainerVersion
getContainerVersion
initEnvironment
initGitIgnore
setContainerVersion

...

BUILD SUCCESSFUL in 897ms
1 actionable task: 1 executed
```
In case you see a similar output you are good to go.

If this is the first time you execute a Gradle build, the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) will download Gradle. This is a one time action that takes a bit more time to complete.

## First Environment
The easiest way to start playing around with KubeRig is to use [Kind](https://github.com/kubernetes-sigs/kind) and bootstrap a Kubernetes cluster locally.

In case you don't have Kind yet, follow the [installation instructions](https://github.com/kubernetes-sigs/kind#installation-and-usage).

Then create a new cluster. In order to create a Kind cluster with Ingress support execute the following command.
```shell
$ ./createKindClusterWithIngressSupport.sh
Creating cluster "kind" ...
 ✓ Ensuring node image (kindest/node:v1.16.3) 🖼 
 ✓ Preparing nodes 📦 
 ✓ Writing configuration 📜 
 ✓ Starting control-plane 🕹️ 
 ✓ Installing CNI 🔌 
 ✓ Installing StorageClass 💾 
Set kubectl context to "kind-kind"
You can now use your cluster with:

kubectl cluster-info --context kind-kind

Have a nice day! 👋
namespace/ingress-nginx created
configmap/nginx-configuration created
configmap/tcp-services created
configmap/udp-services created
serviceaccount/nginx-ingress-serviceaccount created
clusterrole.rbac.authorization.k8s.io/nginx-ingress-clusterrole created
role.rbac.authorization.k8s.io/nginx-ingress-role created
rolebinding.rbac.authorization.k8s.io/nginx-ingress-role-nisa-binding created
clusterrolebinding.rbac.authorization.k8s.io/nginx-ingress-clusterrole-nisa-binding created
deployment.apps/nginx-ingress-controller created
limitrange/ingress-nginx created
service/ingress-nginx created
deployment.apps/nginx-ingress-controller patched
``` 

Now that your local Kubernetes cluster is up and running we can initialize a KubeRig environment so we can start deploying to it.
```shell
$ ./gradlew initEnvironment --currentKubectlContext --name localdev

> Task :initEnvironment
Using current-context: kind-kind
Using server: https://127.0.0.1:43565
Using namespace: default

...certificate details...

Created kuberig service account in namespace default.
Created edit role binding for kuberig service account in namespace default

BUILD SUCCESSFUL in 6s
1 actionable task: 1 executed
```

The localdev environment is now ready for use. Check the KubeRig tasks again and you will see that a number of new tasks are shown specifically for the localdev environment.

> If you leave the encryption key in place, you can execute initEnvironment repeatedly. Especially handy with throw away clusters like the ones you create with Kind. 

```shell
$ ./gradlew tasks --group kuberig

> Task :tasks

------------------------------------------------------------
Tasks runnable from root project
------------------------------------------------------------

Kuberig tasks
-------------
clearContainerVersion
createEncryptionKeyLocaldevEnvironment
decryptConfigLocaldevEnvironment
decryptFileLocaldevEnvironment
decryptLocaldevEnvironment
deployLocaldevEnvironment
encryptConfigLocaldevEnvironment
encryptFileLocaldevEnvironment
encryptLocaldevEnvironment
generateYamlLocaldevEnvironment
getContainerVersion
initEnvironment
initGitIgnore
setContainerVersion
showConfigLocaldevEnvironment

To see all tasks and more detail, run gradlew tasks --all

To see more detail about a task, run gradlew help --task <task>

BUILD SUCCESSFUL in 1s
1 actionable task: 1 executed
```

## First Deployment
The kuberig-starter repository comes with a very basic ingress example. 

We will go into detail about how to define the resources in the [Quick DSL Intro](quick-dsl-intro.md).
 
For now lets just deploy the example.

To execute a deployment to the localdev environment we can use the `deployLocaldevEnvironment` task.

```shell
$ ./gradlew deployLocaldevEnvironment

> Task :deployLocaldevEnvironment
[PROCESSING] kuberig-starter/build/classes/kotlin/main/ingress/IngressExample.class
[TICK-SYSTEM] starting...
[TICK-SYSTEM][TICK#1] deploying 5 resource(s).
------
deploying Pod - foo-app in default namespace...
created Pod - foo-app in default namespace
------
deploying Service - foo-service in default namespace...
created Service - foo-service in default namespace
------
deploying Pod - bar-app in default namespace...
created Pod - bar-app in default namespace
------
deploying Service - bar-service in default namespace...
created Service - bar-service in default namespace
------
deploying Ingress - example-ingress in default namespace...
created Ingress - example-ingress in default namespace
[TICK-SYSTEM] success.

BUILD SUCCESSFUL in 2s
4 actionable tasks: 3 executed, 1 up-to-date
```

Wait for all pods to hit status Running.
```shell
$ kubectl get pods --all-namespaces
NAMESPACE       NAME                                         READY   STATUS    RESTARTS   AGE
default         bar-app                                      1/1     Running   0          10s
default         foo-app                                      1/1     Running   0          10s
ingress-nginx   nginx-ingress-controller-7c9649749b-qdwbm    1/1     Running   0          2m23s
kube-system     coredns-5644d7b6d9-bs5w4                     1/1     Running   0          2m22s
kube-system     coredns-5644d7b6d9-cgvs9                     1/1     Running   0          2m22s
kube-system     etcd-kind-control-plane                      1/1     Running   0          80s
kube-system     kindnet-x67w7                                1/1     Running   0          2m22s
kube-system     kube-apiserver-kind-control-plane            1/1     Running   0          96s
kube-system     kube-controller-manager-kind-control-plane   1/1     Running   0          98s
kube-system     kube-proxy-ctv8x                             1/1     Running   0          2m22s
kube-system     kube-scheduler-kind-control-plane            1/1     Running   0          79s
```

Now we can test the deployed Ingresses.
```shell
$ curl localhost/foo
foo
$ curl localhost/bar
bar
```

Great job you just perform a successful deployment with kuberig.

Now you can proceed to learning about the KubeRig DSL and start defining your own resources.

Don't forget to clean up the Kind cluster.
```shell
$ kind delete cluster
Deleting cluster "kind" ...
```




