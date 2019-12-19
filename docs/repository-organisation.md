# Repository Organisation

A KubeRig repository follows a well defined directory structure.

The root directory is referred to as the project directory. 
- It contains the [Gradle wrapper files](https://docs.gradle.org/current/userguide/gradle_wrapper.html). 
- It contains the `environments` directory.
- It contains the `src\main\kotlin` directory.
- It contains the `.gitignore` file. You can initialize it by executing the `initGitIgnore` task.
- It contains the `settings.gradle.kts` and `build.gradle.kts` Gradle build files.

The `environments` directory contains a sub directory for every environment defined in the build file.

Each `environment` directory contains the following files:
- {environment-name}.keyset.json the encryption key of the environment. You should **NEVER EVER** commit the {environment-name}.keyset.json file. If you have used the `initGitIgnore` task this file will already get ignored.
- .encrypted.{environment-name}.access-token the encrypted version of the JWT access token of service account that can be used to do deployments.
- {environment-name}-config.properties a properties file with environment specific configuration parameters. Can contain encrypted values use the `encryptValue{Environment-name}Environment` task to encrypt values.
- other files that are environment specific, remember to prefix files that contains sensitive information with `.plain.` . 

The `src\main\kotlin` directory is where you place your [Kotlin](https://kotlinlang.org/) resource generation code.

The kuberig extension in the `build.gradle.kts` file allows you to define your environments, target platform and more. 