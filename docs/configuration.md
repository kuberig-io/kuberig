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

## Using a custom KubeRig DSL dependency
In case you are generating a custom KubeRig DSL specifically for your clusters API specification.

You can use `dslDependencyOverride` to use specify it. When you do this the default dependency will not be added.
```kotlin
kuberig {
  dslDependencyOverride = "groupId:artifactId:1.2.3"
}
```