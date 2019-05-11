package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.core.deploy.ResourceDeployer
import eu.rigeldev.kuberig.gradle.config.KubeRigExtension
import org.gradle.api.tasks.TaskAction

open class ResourceDeploymentTask : AbstractResourceTask() {

    @TaskAction
    fun deployResources() {
        val methodResults = this.resourceGeneratorMethodExecutor()
            .execute()

        val resourceDeployer = ResourceDeployer(
            this.project.rootDir,
            this.environment,
            this.project.extensions.getByType(KubeRigExtension::class.java).getDeployControl(),
            this.buildResourceGenerationRuntimeClasspathClassLoader(),
            this.encryptionSupportFactory())

        resourceDeployer.deploy(methodResults)
    }
}