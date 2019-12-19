# Configuration

> work-in-progress

# KubeRig Gradle extension
The KubeRig Gradle extension provides a code block that makes it possible to configure KubeRig.

In case Kubernetes is your target platform you can specify the version like this: 
```kotlin
kuberig {
  kubernetes("v1.16.1")
}
```

In case OpenShift is your target platform:
```kotlin
kuberig {
  openshift("v3.9.0")
}
```