package eu.rigeldev.kuberig.gradle

import eu.rigeldev.kuberig.core.generation.ResourceGenerationDetector
import eu.rigeldev.kuberig.core.generation.ResourceGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

open class ResourceGenerationTask : DefaultTask() {

    @TaskAction
    fun generateResources() {
        val detector = ResourceGenerationDetector()

        val compileKotlin = project.tasks.getByName("compileKotlin") as KotlinCompile

        detector.detectResourceMethods(compileKotlin.getDestinationDir())

        val generator = ResourceGenerator()

        val classpath = mutableListOf<File>()

        val jar = project.tasks.getByName("jar") as Jar

        classpath.add(jar.archiveFile.get().asFile)
        classpath.addAll(project.configurations.getByName("runtimeClasspath").resolve())

        generator.generateResources(
            detector.resourceGeneratorTypes,
            classpath.toList(),
            project.file("build/generated-yaml")
        )
    }
}