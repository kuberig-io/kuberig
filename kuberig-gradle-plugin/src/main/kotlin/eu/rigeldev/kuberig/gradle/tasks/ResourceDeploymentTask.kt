package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.core.deploy.ResourceDeployer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class ResourceDeploymentTask : AbstractResourceTask() {

    private var groupName: String = ""
    private var allGroups: Boolean = false

    fun getGroupName(): String? {
        return this.groupName
    }

    @Option(option = "group", description = "The resource group to deploy. (optional)")
    fun setGroupName(groupName: String) {
        this.groupName = groupName
    }

    fun getAllGroups(): Boolean {
        return this.allGroups
    }

    @Option(option = "allGroups", description = "Trigger deployment for all resource groups.")
    fun setAllGroups(allGroups: Boolean) {
        this.allGroups = allGroups
    }

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