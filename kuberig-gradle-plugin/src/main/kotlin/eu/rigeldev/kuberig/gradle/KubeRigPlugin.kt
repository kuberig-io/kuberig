package eu.rigeldev.kuberig.gradle

import eu.rigeldev.kuberig.config.KubeRigEnvironment
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
        val kotlinVersion = props["kotlin.version"] as String

        val extension = project.extensions.create("kuberig", KubeRigExtension::class.java, project)

        project.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        project.dependencies.add("implementation", "eu.rigeldev.kuberig:kuberig-annotations:$kuberigVersion")
        project.dependencies.add("implementation", "eu.rigeldev.kuberig:kuberig-dsl-base:$kuberigVersion")

        extension.environments.all { environment ->


            project.tasks.register(
                this.taskName("generateYaml", environment),
                ResourceGenerationTask::class.java
            )
            { resourceGenerationTask ->
                resourceGenerationTask.group = "kuberig"
                resourceGenerationTask.dependsOn("jar")
                resourceGenerationTask.kuberigVersion = kuberigVersion
                resourceGenerationTask.environment = environment
            }

            project.tasks.register(
                this.taskName("deploy", environment),
                ResourceDeploymentTask::class.java
            )
            { resourceDeployerTask ->
                resourceDeployerTask.group = "kuberig"
                resourceDeployerTask.dependsOn("jar")
                resourceDeployerTask.kuberigVersion = kuberigVersion
                resourceDeployerTask.environment = environment
            }

            project.tasks.register(
                this.taskName("createEncryptionKey", environment),
                GenerateEncryptionKeyTask::class.java
            ) { generateEncryptionKeyTask ->
                generateEncryptionKeyTask.group = "kuberig"
                generateEncryptionKeyTask.environment = environment

            }

            project.tasks.register(
                this.taskName("encryptValue", environment),
                EncryptEnvironmentValueTask::class.java
            ) {
                it.group = "kuberig"
                it.environment = environment
            }

            project.tasks.register(
                this.taskName("decryptValue", environment),
                DecryptEnvironmentValueTask::class.java
            ) {
                it.group = "kuberig"
                it.environment = environment
            }

            project.tasks.register(
                this.taskName("encryptFile", environment),
                EncryptEnvironmentFileTask::class.java
            ) {
                it.group = "kuberig"
                it.environment = environment
            }

            project.tasks.register(
                this.taskName("decryptFile", environment),
                DecryptEnvironmentFileTask::class.java
            ) {
                it.group = "kuberig"
                it.environment = environment
            }
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
                "eu.rigeldev.kuberig.dsl.$platformTypeName:kuberig-dsl-$platformTypeName-$platformVersion:$kuberigVersion"
            )

            it.dependencies.add(
                "implementation",
                "eu.rigeldev.kuberig:kuberig-core:$kuberigVersion"
            )
        }
    }

    private fun loadProps(): Properties {
        val props = Properties()
        props.load(this.javaClass.getResourceAsStream("/kuberig.properties"))
        return props
    }

    private fun taskName(action: String, environment: KubeRigEnvironment): String {
        val capitalizedEnvironmentName = environment.name.capitalize()

        return "$action${capitalizedEnvironmentName}Environment"
    }
}