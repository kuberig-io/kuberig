package io.kuberig.core.execution

import io.kuberig.core.preparation.InitialResourceInfo
import io.kuberig.dsl.support.ApplyActionOverwrite

interface MethodCallContextProcessor {

    fun process(methodCallContext: MethodCallContext, processor: (initialResourceInfo: InitialResourceInfo, applyActionOverwrite: ApplyActionOverwrite) -> Unit)

}