package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.core.deploy.ResourceDeployer
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorExecutor
import org.gradle.api.tasks.TaskAction

open class ResourceDeploymentTask : AbstractResourceTask() {

    @TaskAction
    fun deployResources() {
        val executor = ResourceGeneratorExecutor(
            this.buildResourceGenerationRuntimeClasspathClassLoader(),
            this.environment
        )

        val deployer = ResourceDeployer(
            this.environment
        )

        super.detectResourceGeneratorMethods()
            .map(executor::execute)
            .map(deployer::deploy)
    }
}