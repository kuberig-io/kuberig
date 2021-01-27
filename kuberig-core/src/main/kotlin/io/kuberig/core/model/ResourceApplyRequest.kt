package io.kuberig.core.model

import io.kuberig.annotations.ApplyAction
import io.kuberig.core.preparation.InitialResourceInfo

/**
 * Executing annotated methods results in resource apply requests.
 *
 * That hold the resource that needs to be applied.
 * What action is desired to apply the resource.
 * The tick the resource needs to be applied in.
 */
data class ResourceApplyRequest(
    val initialResourceInfo: InitialResourceInfo,
    val applyAction: ApplyAction,
    val tick: Int
)
