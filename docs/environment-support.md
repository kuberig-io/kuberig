# Environment Support

Environments are a first-class concept in KubeRig.

Each environment has its own sub-directory in the `environments` directory.

The environment sub-directory contains at least:
- The CA certificate of the cluster.
- The configs properties file.
- The keyset.json encryption key.
- An encrypted access token of the service account that is used to deploy to the environment.

Environment specific information is available during resource generation via the following methods of the `ResourceGenerationContext`:
- `environmentFileText` pass in a relative path to the environment sub-directory.
- `environmentFileBytes` pass in a relative path to the environment sub-directory.
- `environmentConfig` pass in a key available in the configs properties file.

## Environment Specific Tasks

After initializing/creating an environment with the `initEnvironment` task KubeRig provides a number of tasks 
to execution actions specifically for the environment.

### Deploy Task

There is a `deploy` task for every environment. This task will scan

This task will directly communicate with the Kubernetes API to create/update the resources that you have defined using the KubeRig DSL. 

### Generate Yaml task

In case you need it or feel nostalgic there is a `generateYaml` task for every environment.

The files are generated inside the build/generated-yaml directory. For each environment you will find a subdirectory there containing the generated yaml files. 

### Encryption tasks

Various tasks to encrypt/decrypt files an configuration parameters. More information about those can be found on the [encryption support](encryption-support.md) page.

