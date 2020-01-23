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
                ResourceGenerationTask::class.java,
                ResourceTaskConfigurationAction(environment)
            )
            project.tasks.registerEnvironmentTask(
                "deploy",
                environment,
                ResourceDeploymentTask::class.java,
                ResourceTaskConfigurationAction(environment)
            )
            project.tasks.registerEnvironmentTask(
                "encryptConfig",
                environment,
                EncryptEnvironmentConfigTask::class.java,
                EnvironmentTaskConfigurationAction(environment)
            )
            project.tasks.registerEnvironmentTask(
                "decryptConfig",
                environment,
                DecryptEnvironmentConfigTask::class.java,
                EnvironmentTaskConfigurationAction(environment)
            )
            project.tasks.registerEnvironmentTask(
                "encryptFile",
                environment,
                EncryptEnvironmentFileTask::class.java,
                EnvironmentTaskConfigurationAction(environment)
            )
            project.tasks.registerEnvironmentTask(
                "decryptFile",
                environment,
                DecryptEnvironmentFileTask::class.java,
                EnvironmentTaskConfigurationAction(environment)
            )
            project.tasks.registerEnvironmentTask(
                "encrypt",
                environment,
                EncryptEnvironmentTask::class.java,
                EnvironmentTaskConfigurationAction(environment)
            )
            project.tasks.registerEnvironmentTask(
                "decrypt",
                environment,
                DecryptEnvironmentTask::class.java,
                EnvironmentTaskConfigurationAction(environment)
            )
            project.tasks.registerEnvironmentTask(
                "showConfig",
                environment,
                ShowEnvironmentConfigTask::class.java,
                EnvironmentTaskConfigurationAction(environment)
            )
        }

        project.tasks.register("initGitIgnore", InitGitIgnoreTask::class.java) {
            it.group = "kuberig"
        }

        project.tasks.register("initEnvironment", InitEnvironmentTask::class.java) {
            it.group = "kuberig"
        }

        project.tasks.register("setContainerVersion", SetContainerVersionTask::class.java) {
            it.group = "kuberig"
        }

        project.tasks.register("clearContainerVersion", ClearContainerVersion::class.java) {
            it.group = "kuberig"
        }

        project.tasks.register("getContainerVersion", GetContainerVersionTask::class.java) {
            it.group = "kuberig"
        }

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

            it.dependencies.add(
                "implementation",
                "eu.rigeldev.kuberig.dsl.$platformTypeName:kuberig-dsl-$platformTypeName-$platformVersion:$kuberigDslVersion"
            )
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
        }
    }



    private fun createEnvironmentsFromDirectories(project: Project) {
        val evaluatedExtension = project.extensions.getByType(KubeRigExtension::class.java)
        val environmentsContainer = evaluatedExtension.environments.asMap

        val environmentsDirectory = project.file("environments")
        if (environmentsDirectory.exists()) {

            val fileOrDirectoryList = environmentsDirectory.listFiles()
            for (fileOrDirectory in fileOrDirectoryList) {
                if (fileOrDirectory.isDirectory) {

                    val environmentName = fileOrDirectory.name

                    if (!environmentsContainer.containsKey(environmentName)) {

                        evaluatedExtension.environments.create(environmentName)
                    }

                }
            }

        }
    }
}