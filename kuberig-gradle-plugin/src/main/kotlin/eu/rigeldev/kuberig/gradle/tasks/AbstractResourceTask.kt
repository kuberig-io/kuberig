package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.core.execution.*
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.net.URLClassLoader

abstract class AbstractResourceTask : AbstractEncryptionSupportTask() {

    protected fun resourceGeneratorMethodExecutor(groupNameMatcher: ResourceGroupNameMatcher) : ResourceGeneratorExecutor {
        val compileKotlin = project.tasks.getByName("compileKotlin") as KotlinCompile

        return ResourceGeneratorExecutor(
            compileKotlin.getDestinationDir(),
            compileKotlin.classpath.files.toSet(),
            this.buildResourceGenerationRuntimeClasspathClassLoader(),
            this.environment,
            this.environmentFileSystem(),
            groupNameMatcher
        )
    }

    protected fun groupNameMatcher(groupName: String, allGroups: Boolean): ResourceGroupNameMatcher {
        return when {
            allGroups -> {
                AlwaysResourceGroupNameMatcher()
            }
            groupName != "" -> {
                RequestedResourceGroupNameMatcher(groupName)
            }
            else -> {
                NoResourceGroupNameMatcher()
            }
        }
    }

    /**
     * The resource generation code runtime classpath has a couple of dependencies like:
     * - kuberig-annotations
     * - kuberig-core
     * - kuberig-dsl-base (a transitive dependency of a kuberig-dsl-* )
     *
     * That are also available in the gradle build classpath.
     *
     * It is important that these shared dependencies are used from the gradle build classpath in order to
     * preserve class equality.
     */
    protected fun buildResourceGenerationRuntimeClasspathClassLoader() : ClassLoader {
        val jar = project.tasks.getByName("jar") as Jar

        val runtimeClasspath = project.configurations.getByName("runtimeClasspath")

        val completeRuntimeClasspath = mutableListOf<File>()
        completeRuntimeClasspath.add(jar.archiveFile.get().asFile)
        completeRuntimeClasspath.addAll(runtimeClasspath.resolve())

        val kuberigVersion = this.kubeRigExtension().kuberigVersion()

        val filteredRuntimeClasspath = completeRuntimeClasspath
            .filter { it.name != "kuberig-annotations-$kuberigVersion.jar" }
            .filter { it.name != "kuberig-core-$kuberigVersion.jar" }
            .filter { it.name != "kuberig-dsl-base-$kuberigVersion.jar" }
            .filter {!it.name.startsWith("kotlin-")}
            .filter {!it.name.startsWith("jackson-")}

        val urls = filteredRuntimeClasspath.map { it.toURI().toURL() }

        return URLClassLoader(
            urls.toTypedArray(),
            Thread.currentThread().contextClassLoader
        )
    }
}