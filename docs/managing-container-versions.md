# Managing Container Versions

KubeRig provides tasks that allow you to manage container versions/tags globally for all environments or for a specific environment.

#  `setContainerVersion`

Add/Update a container version. The environment parameter is optional.

```shell
$ ./gradlew help --task setContainerVersion

> Task :help
Detailed task information for setContainerVersion

Path
     :setContainerVersion

Type
     SetContainerVersionTask (eu.rigeldev.kuberig.gradle.tasks.SetContainerVersionTask)

Options
     --containerAlias     The container alias to add/update the container version for.

     --containerVersion     The container version.

     --environment     The name of the environment to add/update the container version for.

Description
     -

Group
     kuberig

BUILD SUCCESSFUL in 914ms
1 actionable task: 1 executed
```

# `clearContainerVersion`

Clear a container version. Can be used to remove a version override for a specific environment.

```shell
$ ./gradlew help --task clearContainerVersion

> Task :help
Detailed task information for clearContainerVersion

Path
     :clearContainerVersion

Type
     ClearContainerVersion (eu.rigeldev.kuberig.gradle.tasks.ClearContainerVersion)

Options
     --containerAlias     The container alias to add/update the container version for.

     --environment     The name of the environment to add/update the container version for.

Description
     -

Group
     kuberig

BUILD SUCCESSFUL in 869ms
1 actionable task: 1 executed
```

# `getContainerVersion`

Shows the version that will be used. Can be used to check if there is a version override for a specific environment.

```shell
$ ./gradlew help --task getContainerVersion

> Task :help
Detailed task information for getContainerVersion

Path
     :getContainerVersion

Type
     GetContainerVersionTask (eu.rigeldev.kuberig.gradle.tasks.GetContainerVersionTask)

Options
     --containerAlias     The container alias to add/update the container version for.

     --environment     The name of the environment to add/update the container version for.

Description
     -

Group
     kuberig

BUILD SUCCESSFUL in 830ms
1 actionable task: 1 executed

```