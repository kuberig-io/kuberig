# KubeRig Example

This example demonstrates a build file that defines 2 environments
- local, which points to an api-server running on localhost, it was tested with microk8s.
- dev, which points to an fictive remote api-server, just for the sake of showing how to define multiple environments.

if you run 
```bash
./gradlew tasks --group kuberig
```
You will notice that for each defined environment you get tasks that let you
- deploy{environment name}Environment - Deploys all resources to the environment
- generateYaml{environment name}Environment - Just because we can, 
or if you want to figure out what is actually going to be deployed or diagnose a generation issue.

If you have microk8s running you can execute the deployLocalEnvironment task and start playing with it.

