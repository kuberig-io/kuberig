package io.kuberig.gradle.tasks.resource

import io.kuberig.core.GenerateYaml
import io.kuberig.gradle.tasks.AbstractResourceTask
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

open class ResourceGenerationTask : AbstractResourceTask() {

    @TaskAction
    fun generateResources() {
        val compileKotlin = project.tasks.getByName("compileKotlin") as KotlinCompile
        val kubeRigExtension = this.kubeRigExtension()

        GenerateYaml()
            .rootFileSystem(kubeRigExtension.rootFileSystem())
            .groupNameMatcher(groupName, allGroups)
            .flags(kubeRigExtension.flags)
            .environment(environment)
            .compileOutputDirectory(compileKotlin.destinationDir)
            .resourceGenerationCompileClasspath(compileKotlin.classpath.files.toSet())
            .resourceGenerationRuntimeClasspath(buildResourceGenerationRuntimeClasspathClassLoader())
            .yamlOutputFileConvention(kubeRigExtension.yamlOutputFileConvention)
            .execute()
    }

}