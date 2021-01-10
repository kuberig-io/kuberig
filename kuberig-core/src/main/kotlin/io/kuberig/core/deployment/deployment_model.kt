package io.kuberig.core.deployment

import io.kuberig.annotations.ApplyAction
import io.kuberig.dsl.model.FullResource

data class DeploymentPlan(val ticks: List<DeploymentTick>)

data class DeploymentTick(val tickNumber: Int, val deploymentTasks: List<DeploymentTask>)

data class DeploymentTask(val sourceLocation: String,
                          val resource: FullResource,
                          val applyAction: ApplyAction
)