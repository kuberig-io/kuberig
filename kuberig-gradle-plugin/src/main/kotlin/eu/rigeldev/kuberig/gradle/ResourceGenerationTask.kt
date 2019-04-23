package eu.rigeldev.kuberig.gradle

import eu.rigeldev.kuberig.core.generation.ResourceGenerationDetector
import eu.rigeldev.kuberig.core.generation.ResourceGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

open class ResourceGenerationTask : DefaultTask() {

    init {
        println("A new resource generation task was created.")
    }

    @TaskAction
    fun generateResources() {
        val detector = ResourceGenerationDetector()

        val compileKotlin = project.tasks.getByName("compileKotlin") as KotlinCompile

        detector.detectResourceMethods(compileKotlin.getDestinationDir())

        val generator = ResourceGenerator()

        generator.generateResources(
            detector.resourceGeneratorTypes,
            project.file("build/generated-yaml")
        )
    }
}