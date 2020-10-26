package io.kuberig.gradle.tasks.resource

import io.kuberig.core.deployment.ResourceDeployer
import io.kuberig.gradle.tasks.AbstractResourceTask
import org.gradle.api.tasks.TaskAction

open class ResourceDeploymentTask : AbstractResourceTask() {

    @TaskAction
    fun deployResources() {
        val groupNameMatcher = this.groupNameMatcher(groupName, allGroups)
        val methodResults = this.resourceGeneratorMethodExecutor(groupNameMatcher)
            .execute()

        val kubeRigExtension = this.kubeRigExtension()

        val resourceDeployer = ResourceDeployer(
            kubeRigExtension.flags,
            this.environmentFileSystem(),
            kubeRigExtension.getDeployControl(),
            this.buildResourceGenerationRuntimeClasspathClassLoader())

        resourceDeployer.deploy(methodResults)
    }
}