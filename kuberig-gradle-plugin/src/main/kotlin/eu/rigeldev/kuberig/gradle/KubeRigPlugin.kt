package eu.rigeldev.kuberig.gradle

import eu.rigeldev.kuberig.gradle.config.KubeRigExtension
import eu.rigeldev.kuberig.gradle.tasks.*
import eu.rigeldev.kuberig.gradle.tasks.encryption.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

open class KubeRigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply("java")
        project.plugins.apply("idea")
        project.plugins.apply("org.jetbrains.kotlin.jvm")


        val props = this.loadProps()
        val kuberigVersion = props["kuberig.version"] as String
        val kuberigDslVersion = props["kuberig.dsl.version"] as String
        val kotlinVersion = props["kotlin.version"] as String

        val extension = project.extensions.create("kuberig", KubeRigExtension::class.java, project)

        project.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        project.dependencies.add("implementation", "eu.rigeldev.kuberig:kuberig-annotations:$kuberigVersion")
        project.dependencies.add("implementation", "eu.rigeldev.kuberig:kuberig-dsl-base:$kuberigDslVersion")

        extension.environments.all { environment ->


            project.tasks.registerEnvironmentTask(
                "generateYaml",
                environment,
                ResourceGenerationTask::class.java,
                ResourceTaskConfigurationAction(environment, kuberigVersion)
            )
            project.tasks.registerEnvironmentTask(
                "deploy",
                environment,
                ResourceDeploymentTask::class.java,
                ResourceTaskConfigurationAction(environment, kuberigVersion)
            )
            project.tasks.registerEnvironmentTask(
                "createEncryptionKey",
                environment,
                GenerateEncryptionKeyTask::class.java,
                EnvironmentTaskConfigurationAction(environment)
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

        project.tasks.register("initEnvironment", eu.rigeldev.kuberig.gradle.tasks.InitEnvironmentTask::class.java) {
            it.group = "kuberig"
        }

        project.tasks.withType(KotlinCompile::class.java) {
            it.kotlinOptions.jvmTarget = "1.8"
        }

        project.afterEvaluate {
            val evaluatedExtension = it.extensions.getByType(KubeRigExtension::class.java)

            val platformTypeName = evaluatedExtension.targetPlatform.platform.name.toLowerCase()
            val platformVersion = evaluatedExtension.targetPlatform.platformVersion

            it.dependencies.add(
                "implementation",
                "eu.rigeldev.kuberig.dsl.$platformTypeName:kuberig-dsl-$platformTypeName-$platformVersion:$kuberigDslVersion"
            )

            it.dependencies.add(
                "implementation",
                "eu.rigeldev.kuberig:kuberig-core:$kuberigVersion"
            )

            this.createEnvironmentsFromDirectories(project)
        }
    }

    private fun loadProps(): Properties {
        val props = Properties()
        props.load(this.javaClass.getResourceAsStream("/kuberig.properties"))
        return props
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