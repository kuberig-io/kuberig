package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.core.deploy.ResourceDeployer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

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