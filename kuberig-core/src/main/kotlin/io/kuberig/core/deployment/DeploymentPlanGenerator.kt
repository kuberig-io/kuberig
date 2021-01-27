package io.kuberig.core.deployment

import io.kuberig.core.model.SuccessResult
import io.kuberig.core.preparation.ResourcePreparation

class DeploymentPlanGenerator(private val resourcePreparation: ResourcePreparation) {

    fun generateDeploymentPlan(generatorResults: List<SuccessResult>): DeploymentPlan {
        val tasksByTick = mutableMapOf<Int, MutableList<DeploymentTask>>()

        for (generatorResult in generatorResults) {
            val sourceMethod = generatorResult.fullMethodName()

            for (resourceApplyRequest in generatorResult.resourceApplyRequests) {
                val tickNumber = resourceApplyRequest.tick

                val tickTasks = tasksByTick.getOrDefault(tickNumber, mutableListOf())

                val rawJsonResourceInfo = resourcePreparation.prepare(resourceApplyRequest.initialResourceInfo)

                tickTasks.add(DeploymentTask(sourceMethod, rawJsonResourceInfo, resourceApplyRequest.applyAction))

                tasksByTick[tickNumber] = tickTasks
            }
        }

        val ticks = mutableListOf<DeploymentTick>()

        for (tickTasks in tasksByTick) {
            val tasks = tickTasks.value

            val customResourceDeploymentTasks = tasks.filter { it.rawJsonResourceInfo.kind.toLowerCase() == "customresourcedefinition" }
            val deploymentTasks = tasks.filter { it.rawJsonResourceInfo.kind.toLowerCase() != "customresourcedefinition" }

            val deploymentTick = DeploymentTick(tickTasks.key, customResourceDeploymentTasks, deploymentTasks)

            ticks.add(deploymentTick)
        }

        return DeploymentPlan(ticks)
    }

}