package io.kuberig.core.deployment

import io.kuberig.annotations.ApplyAction
import io.kuberig.core.resource.RawResourceInfo

data class DeploymentPlan(val ticks: List<DeploymentTick>)

data class DeploymentTick(val tickNumber: Int, val deploymentTasks: List<DeploymentTask>)

data class DeploymentTask(val sourceLocation: String,
                          val rawResourceInfo: RawResourceInfo,
                          val applyAction: ApplyAction
)