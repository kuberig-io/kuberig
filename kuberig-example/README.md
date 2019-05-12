# KubeRig Example

This example demonstrates a build file that defines 2 environments
- a 'local' environment, which points to an api-server running on localhost, it was tested with microk8s.
- a 'dev' environment, which points to an fictive remote api-server, just for the sake of showing how to define multiple environments.

if you run 
```bash
./gradlew tasks --group kuberig
```
You will notice that for each defined environment you get tasks:
- deploy{environment name}Environment - Deploys all applicable resources to the environment
- generateYaml{environment name}Environment - Just because we can, 
or if you want to figure out what is actually going to be deployed or diagnose a generation issue.
- encryption tasks - to encrypt sensitive information
- decryption tasks - te decrypt sensitive information

If you have microk8s running you can execute the deployLocalEnvironment task and start playing with it.

## WARNING

In this example the environment encryption keys are committed:

- environments/local/local.keyset
- environments/dev/dev.keyset

This is only done for demo purposes. You should NEVER EVER do this!