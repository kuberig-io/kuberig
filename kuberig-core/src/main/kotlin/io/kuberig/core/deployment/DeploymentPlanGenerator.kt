package io.kuberig.core.deployment

import io.kuberig.core.model.SuccessResult

class DeploymentPlanGenerator {

    fun generateDeploymentPlan(generatorResults: List<SuccessResult>): DeploymentPlan {
        val tasksByTick = mutableMapOf<Int, MutableList<DeploymentTask>>()

        for (generatorResult in generatorResults) {
            val sourceMethod = generatorResult.fullMethodName()

            for (resourceApplyRequest in generatorResult.resourceApplyRequests) {
                val tickNumber = resourceApplyRequest.tick

                val tickTasks = tasksByTick.getOrDefault(tickNumber, mutableListOf())

                tickTasks.add(DeploymentTask(sourceMethod, resourceApplyRequest.rawResourceInfo, resourceApplyRequest.applyAction))

                tasksByTick[tickNumber] = tickTasks
            }
        }

        val ticks = mutableListOf<DeploymentTick>()

        for (tickTasks in tasksByTick) {
            ticks.add(DeploymentTick(tickTasks.key, tickTasks.value.toList()))
        }

        return DeploymentPlan(ticks)
    }

}