package io.kuberig.core

import io.kuberig.core.deployment.DeploymentPlanGenerator
import io.kuberig.core.deployment.ResourceDeployer
import io.kuberig.core.deployment.control.TickInfo
import io.kuberig.core.execution.ResourceGeneratorExecutor
import io.kuberig.core.preparation.InitialResourceInfoFactory
import io.kuberig.core.preparation.ResourcePreparation
import io.kuberig.core.resource.EnvYamlSourceService

class Deploy : HighLevelResourceAction<Deploy>() {

    private lateinit var tickInfo: TickInfo

    fun tickInfo(tickInfo: TickInfo): Deploy {
        this.tickInfo = tickInfo

        return this
    }

    override fun execute() {
        val environmentFileSystem = rootFileSystem.environment(environment.name)

        val initialResourceInfoFactory = InitialResourceInfoFactory()

        val resourceGeneratorExecutor = ResourceGeneratorExecutor(
            compileOutputDirectory,
            resourceGenerationCompileClasspath,
            resourceGenerationRuntimeClasspath,
            environment,
            environmentFileSystem,
            groupNameMatcher,
            initialResourceInfoFactory,
            EnvYamlSourceService(initialResourceInfoFactory, rootFileSystem.repoRootDir)
        )

        val methodResults = resourceGeneratorExecutor.execute()

        val resourcePreparation = ResourcePreparation(environmentFileSystem, flags, methodResults)

        val deploymentPlanGenerator = DeploymentPlanGenerator(resourcePreparation)

        val deploymentPlan = deploymentPlanGenerator.generateDeploymentPlan(methodResults)

        val resourceDeployer = ResourceDeployer(
            flags,
            environmentFileSystem,
            tickInfo,
            resourceGenerationRuntimeClasspath
        )

        resourceDeployer.execute(deploymentPlan)
    }
}