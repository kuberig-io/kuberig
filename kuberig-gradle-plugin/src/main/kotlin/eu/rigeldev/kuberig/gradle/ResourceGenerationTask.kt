package eu.rigeldev.kuberig.gradle

import eu.rigeldev.kuberig.core.generation.ResourceGenerationDetector
import eu.rigeldev.kuberig.core.generation.ResourceGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.net.URLClassLoader

open class ResourceGenerationTask : DefaultTask() {

    var kuberigVersion : String = ""

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
            project.file("build/generated-yaml"),
            this.buildResourceGenerationRuntimeClasspathClassLoader()
        )
    }

    /**
     * The resource generation code runtime classpath has a couple of dependencies like:
     * - kuberig-annotations
     * - kuberig-dsl-base (a transitive dependency of a kuberig-dsl-* )
     *
     * That are also available in the gradle build classpath.
     *
     * It is important that these shared dependencies are used from the gradle build classpath in order to
     * preserve class equality.
     */
    private fun buildResourceGenerationRuntimeClasspathClassLoader() : ClassLoader {
        val jar = project.tasks.getByName("jar") as Jar

        val runtimeClasspath = project.configurations.getByName("runtimeClasspath")

        val completeRuntimeClasspath = mutableListOf<File>()
        completeRuntimeClasspath.add(jar.archiveFile.get().asFile)
        completeRuntimeClasspath.addAll(runtimeClasspath.resolve())

        val filteredRuntimeClasspath = completeRuntimeClasspath
            .filter { it.name != "kuberig-annotations-$kuberigVersion.jar" }
            .filter { it.name != "kuberig-dsl-base-$kuberigVersion.jar" }
            .filter{!it.name.startsWith("kotlin-")}
            .filter{!it.name.startsWith("jackson-")}

        val urls = filteredRuntimeClasspath.map { it.toURI().toURL() }

        return URLClassLoader(
            urls.toTypedArray(),
            Thread.currentThread().contextClassLoader
        )
    }
}