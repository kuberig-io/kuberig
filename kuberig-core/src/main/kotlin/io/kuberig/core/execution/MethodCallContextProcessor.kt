package io.kuberig.core.execution

import io.kuberig.dsl.model.FullResource

interface MethodCallContextProcessor {

    fun process(methodCallContext: MethodCallContext, resources: MutableList<FullResource>)

}