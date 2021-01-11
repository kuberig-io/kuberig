package io.kuberig.core

import io.kuberig.core.deployment.DeploymentPlanGenerator
import io.kuberig.core.deployment.ResourceDeployer
import io.kuberig.core.deployment.control.TickInfo
import io.kuberig.core.execution.ResourceGeneratorExecutor
import io.kuberig.core.resource.EnvYamlSourceService
import io.kuberig.core.resource.RawResourceFactory

class Deploy : HighLevelResourceAction<Deploy>() {

    private lateinit var tickInfo: TickInfo

    fun tickInfo(tickInfo: TickInfo): Deploy {
        this.tickInfo = tickInfo

        return this
    }

    override fun execute() {
        val environmentFileSystem = rootFileSystem.environment(environment.name)

        val rawResourceFactory = RawResourceFactory(environmentFileSystem.defaultNamespace())

        val resourceGeneratorExecutor = ResourceGeneratorExecutor(
            compileOutputDirectory,
            resourceGenerationCompileClasspath,
            resourceGenerationRuntimeClasspath,
            environment,
            environmentFileSystem,
            groupNameMatcher,
            rawResourceFactory,
            EnvYamlSourceService(rawResourceFactory, rootFileSystem)
        )

        val methodResults = resourceGeneratorExecutor.execute()


        val deploymentPlanGenerator = DeploymentPlanGenerator()

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