package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.dsl.model.FullResource

interface MethodCallContextProcessor {

    fun process(methodCallContext: MethodCallContext, resources: MutableList<FullResource>)

}