package io.kuberig.core.execution

import io.kuberig.core.resource.RawResourceInfo
import io.kuberig.dsl.support.ApplyActionOverwrite

interface MethodCallContextProcessor {

    fun process(methodCallContext: MethodCallContext, processor: (rawResourceInfo: RawResourceInfo, applyActionOverwrite: ApplyActionOverwrite) -> Unit)

}