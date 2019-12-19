# Quick Start

The fastest way to get started is by cloning or downloading the [kuberig-empty](https://github.com/teyckmans/kuberig-empty) repository.

## Cloning
Execute git clone, change into the git clone directory and check that the KubeRig tasks are available.  
```shell
git clone https://github.com/teyckmans/kuberig-empty
```

## Downloading
Download the [kuberig-empty zip](https://github.com/teyckmans/kuberig-empty/archive/master.zip) extract the zip and open a terminal in the extracted directory.

## Prerequisites
As KubeRig is a Gradle plugin, it needs a [Java JDK or JRE](http://www.oracle.com/technetwork/java/javase/downloads/index.html) version 8 or higher to be installed. To check, run `java -version`:
```shell
$ java -version 
java version "1.8.0_121"
```

## Check
Open a terminal in the git clone directory or extracted zip directory and check that the KubeRig tasks are available.

```shell
$ cd kuberig-empty
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

If this is the first time you execute a Gradle build, the Gradle wrapper will download Gradle. This is a one time action that takes a bit more time to complete.

## First Environment
The easiest way to start playing around with KubeRig is to use [Kind](https://github.com/kubernetes-sigs/kind) and bootstrap a Kubernetes cluster locally.

In case you don't have Kind yet, follow the [installation instructions](https://github.com/kubernetes-sigs/kind#installation-and-usage).

Then create a new cluster
```shell
$ kind create cluster

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

Have a question, bug, or feature request? Let us know! https://kind.sigs.k8s.io/#community 🙂
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
