package io.kuberig.core.execution

import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.FullResource
import io.kuberig.dsl.support.ApplyActionOverwrite

interface MethodCallContextProcessor {

    fun process(methodCallContext: MethodCallContext, processor: (dslType: KubernetesResourceDslType<FullResource>, applyActionOverwrite: ApplyActionOverwrite) -> Unit)

}