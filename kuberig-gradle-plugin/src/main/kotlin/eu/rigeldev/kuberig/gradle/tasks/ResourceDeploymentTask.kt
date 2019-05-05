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

        val methodResults = super.detectResourceGeneratorMethods()
            .map(executor::execute)

        super.reportAndFailOnErrors(methodResults)

        methodResults
            .map(deployer::deploy)
    }
}