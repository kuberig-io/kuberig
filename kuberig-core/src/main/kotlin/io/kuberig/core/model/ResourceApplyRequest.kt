package io.kuberig.core.model

import io.kuberig.annotations.ApplyAction
import io.kuberig.dsl.model.FullResource

data class ResourceApplyRequest(
    val resource: FullResource,
    val applyAction: ApplyAction,
    val tick: Int
)
