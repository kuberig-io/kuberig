package io.kuberig.gradle.tasks

import io.kuberig.gradle.tasks.encryption.AbstractEncryptionSupportTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.net.URLClassLoader

abstract class AbstractResourceTask : AbstractEncryptionSupportTask() {

    var groupName: String = ""
        @Input
        get
        @Option(option = "group", description = "The resource group to deploy. (optional)")
        set

    var allGroups: Boolean = false
        @Input
        get
        @Option(option = "allGroups", description = "Trigger deployment for all resource groups.")
        set

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