package eu.rigeldev.kuberig.gradle

import org.gradle.api.Action
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
        val kubeRigVersion = props["kuberig.version"]
        val kotlinVersion = props["kotlin.version"]

        project.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        project.dependencies.add("implementation", "eu.rigeldev.kuberig:kuberig-annotations:$kubeRigVersion")
        project.dependencies.add("implementation", "eu.rigeldev.kuberig:kuberig-dsl-base:$kubeRigVersion")

        project.tasks.register("generateResources", ResourceGenerationTask::class.java
        ) {resourceGenerationTask ->
            resourceGenerationTask.group = "kuberig"
            resourceGenerationTask.dependsOn("compileKotlin")
        }

        project.tasks.withType(KotlinCompile::class.java) {
            it.kotlinOptions.jvmTarget = "1.8"
        }

    }

    private fun loadProps() : Properties {
        val props = Properties()
        props.load(this.javaClass.getResourceAsStream("/kuberig.properties"))
        return props
    }
}