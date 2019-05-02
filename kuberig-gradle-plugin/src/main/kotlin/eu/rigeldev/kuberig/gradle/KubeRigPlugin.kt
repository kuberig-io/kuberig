package eu.rigeldev.kuberig.gradle

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

        project.extensions.create("kuberig", KubeRigExtension::class.java)

        project.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        project.dependencies.add("implementation", "eu.rigeldev.kuberig:kuberig-annotations:$kuberigVersion")
        project.dependencies.add("implementation", "eu.rigeldev.kuberig:kuberig-dsl-base:$kuberigVersion")

        project.tasks.register("generateResources", ResourceGenerationTask::class.java
        ) {resourceGenerationTask ->
            resourceGenerationTask.group = "kuberig"
            resourceGenerationTask.dependsOn("jar")
            resourceGenerationTask.kuberigVersion = kuberigVersion
        }

        project.tasks.withType(KotlinCompile::class.java) {
            it.kotlinOptions.jvmTarget = "1.8"
        }

        project.afterEvaluate {
            val extension = it.extensions.getByType(KubeRigExtension::class.java)

            val platformTypeName = extension.targetPlatform.platform.name.toLowerCase()
            val platformVersion = extension.targetPlatform.plafformVersion.versionText()

            it.dependencies.add(
                "implementation",
                "eu.rigeldev.kuberig.dsl.$platformTypeName:kuberig-dsl-$platformTypeName-$platformVersion:$kubeRigVersion"
            )
        }
    }

    private fun loadProps() : Properties {
        val props = Properties()
        props.load(this.javaClass.getResourceAsStream("/kuberig.properties"))
        return props
    }
}