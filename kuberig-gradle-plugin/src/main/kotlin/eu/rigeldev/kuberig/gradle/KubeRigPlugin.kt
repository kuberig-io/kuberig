package eu.rigeldev.kuberig.gradle

import eu.rigeldev.kuberig.gradle.config.KubeRigExtension
import eu.rigeldev.kuberig.gradle.tasks.*
import eu.rigeldev.kuberig.gradle.tasks.encryption.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

open class KubeRigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply("java")
        project.plugins.apply("idea")
        project.plugins.apply("org.jetbrains.kotlin.jvm")

        val extension = project.extensions.create("kuberig", KubeRigExtension::class.java, project)

        extension.environments.all { environment ->

            project.tasks.registerEnvironmentTask(
                    "generateYaml",
                    environment,
                    ResourceGenerationTask::class.java
            )
            project.tasks.registerEnvironmentTask(
                    "deploy",
                    environment,
                    ResourceDeploymentTask::class.java
            )
            project.tasks.registerEnvironmentTask(
                    "createEncryptionKey",
                    environment,
                    GenerateEncryptionKeyTask::class.java
            )
            project.tasks.registerEnvironmentTask(
                    "encryptConfig",
                    environment,
                    EncryptEnvironmentConfigTask::class.java
            )
            project.tasks.registerEnvironmentTask(
                    "decryptConfig",
                    environment,
                    DecryptEnvironmentConfigTask::class.java
            )
            project.tasks.registerEnvironmentTask(
                    "encryptFile",
                    environment,
                    EncryptEnvironmentFileTask::class.java
            )
            project.tasks.registerEnvironmentTask(
                    "decryptFile",
                    environment,
                    DecryptEnvironmentFileTask::class.java
            )
            project.tasks.registerEnvironmentTask(
                    "encrypt",
                    environment,
                    EncryptEnvironmentTask::class.java
            )
            project.tasks.registerEnvironmentTask(
                    "decrypt",
                    environment,
                    DecryptEnvironmentTask::class.java
            )
            project.tasks.registerEnvironmentTask(
                    "showConfig",
                    environment,
                    ShowEnvironmentConfigTask::class.java
            )
        }

        project.tasks.register("initGitIgnore", InitGitIgnoreTask::class.java)
        project.tasks.register("initEnvironment", InitEnvironmentTask::class.java)
        project.tasks.register("setContainerVersion", SetContainerVersionTask::class.java)
        project.tasks.register("clearContainerVersion", ClearContainerVersion::class.java)
        project.tasks.register("getContainerVersion", GetContainerVersionTask::class.java)

        project.tasks.withType(KotlinCompile::class.java) {
            it.kotlinOptions.jvmTarget = "1.8"
        }

        project.afterEvaluate {
            val evaluatedExtension = it.extensions.getByType(KubeRigExtension::class.java)

            val platformTypeName = evaluatedExtension.targetPlatform.platform.name.toLowerCase()
            val platformVersion = evaluatedExtension.targetPlatform.platformVersion

            val kuberigDslVersion = evaluatedExtension.kuberigDslVersion()
            val kuberigVersion = evaluatedExtension.kuberigVersion()
            val kotlinVersion = evaluatedExtension.kotlinVersion()

            if (evaluatedExtension.dslDependencyOverride != null) {
                it.dependencies.add(
                        "implementation",
                        evaluatedExtension.dslDependencyOverride!!
                )
            } else {
                it.dependencies.add(
                        "implementation",
                        "eu.rigeldev.kuberig.dsl.$platformTypeName:kuberig-dsl-$platformTypeName-$platformVersion:$kuberigDslVersion"
                )
            }
            it.dependencies.add(
                    "implementation",
                    "eu.rigeldev.kuberig:kuberig-dsl-base:$kuberigDslVersion"
            )
            it.dependencies.add(
                    "implementation",
                    "eu.rigeldev.kuberig:kuberig-core:$kuberigVersion"
            )
            it.dependencies.add(
                    "implementation",
                    "eu.rigeldev.kuberig:kuberig-annotations:$kuberigVersion"
            )
            it.dependencies.add(
                    "implementation",
                    "eu.rigeldev.kuberig:kuberig-dsl-support:$kuberigVersion"
            )
            it.dependencies.add(
                    "implementation",
                    "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
            )

            this.createEnvironmentsFromDirectories(project)

            project.tasks.withType(ResourceGenerationTask::class.java).forEach { resourceGenerationTask ->
                resourceGenerationTask.dependsOn("build")
            }
            project.tasks.withType(ResourceDeploymentTask::class.java).forEach { resourceDeploymentTask ->
                resourceDeploymentTask.dependsOn("build")
            }
        }
    }

    private fun createEnvironmentsFromDirectories(project: Project) {
        val evaluatedExtension = project.extensions.getByType(KubeRigExtension::class.java)
        val environmentsContainer = evaluatedExtension.environments.asMap

        val rootFileSystem = evaluatedExtension.rootFileSystem()

        rootFileSystem.environments.environments.keys
                .forEach { environmentName ->
                    if (!environmentsContainer.containsKey(environmentName)) {
                        evaluatedExtension.environments.create(environmentName)
                    }
                }
    }
}