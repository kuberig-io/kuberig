package io.kuberig.gradle.tasks.resource

import io.kuberig.core.Deploy
import io.kuberig.gradle.tasks.AbstractResourceTask
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

open class ResourceDeploymentTask : AbstractResourceTask() {

    @TaskAction
    fun deployResources() {
        val compileKotlin = project.tasks.getByName("compileKotlin") as KotlinCompile
        val kubeRigExtension = this.kubeRigExtension()

        Deploy()
            .rootFileSystem(kubeRigExtension.rootFileSystem())
            .groupNameMatcher(groupName, allGroups)
            .flags(kubeRigExtension.flags)
            .tickInfo(kubeRigExtension.getTickInfo())
            .environment(environment)
            .compileOutputDirectory(compileKotlin.destinationDir)
            .resourceGenerationCompileClasspath(compileKotlin.classpath.files.toSet())
            .resourceGenerationRuntimeClasspath(buildResourceGenerationRuntimeClasspathClassLoader())
            .execute()
    }
}