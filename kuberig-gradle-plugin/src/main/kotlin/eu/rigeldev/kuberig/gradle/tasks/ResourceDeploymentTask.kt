package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.core.deploy.ResourceDeployer
import org.gradle.api.tasks.TaskAction

open class ResourceDeploymentTask : AbstractResourceTask() {

    @TaskAction
    fun deployResources() {
        val executor = this.resourceGeneratorMethodExecutor()

        val deployer = ResourceDeployer(
            this.environment
        )

        super.detectResourceGeneratorMethods()
            .map(executor::execute)
            .map(deployer::deploy)
    }
}