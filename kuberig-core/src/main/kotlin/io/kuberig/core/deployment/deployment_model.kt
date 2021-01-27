package io.kuberig.core.deployment

import io.kuberig.annotations.ApplyAction
import io.kuberig.core.resource.RawJsonResourceInfo

data class DeploymentPlan(val ticks: List<DeploymentTick>)

/**
 * In a tick the CRD definitions are deployed first. This is done in order to know if the resources of those types are namespaced or not.
 */
data class DeploymentTick(val tickNumber: Int,
                          /**
                           * CRD definitions.
                           */
                          val crdDefinitionDeploymentTasks: List<DeploymentTask>,
                          /**
                           * Other resources.
                           */
                          val otherDeploymentTasks: List<DeploymentTask>)

data class DeploymentTask(val sourceLocation: String,
                          val rawJsonResourceInfo: RawJsonResourceInfo,
                          val applyAction: ApplyAction
)