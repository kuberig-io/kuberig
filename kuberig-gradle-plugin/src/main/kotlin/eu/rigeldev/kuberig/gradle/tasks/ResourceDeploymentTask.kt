package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.core.deploy.ResourceDeployer
import org.gradle.api.tasks.TaskAction

open class ResourceDeploymentTask : AbstractResourceTask() {

    @TaskAction
    fun deployResources() {
        val methodResults = this.resourceGeneratorMethodExecutor()
            .execute()

        val resourceDeployer = ResourceDeployer(
            this.kubeRigExtension.flags,
            this.environmentFileSystem(),
            this.kubeRigExtension.getDeployControl(),
            this.buildResourceGenerationRuntimeClasspathClassLoader())

        resourceDeployer.deploy(methodResults)
    }
}