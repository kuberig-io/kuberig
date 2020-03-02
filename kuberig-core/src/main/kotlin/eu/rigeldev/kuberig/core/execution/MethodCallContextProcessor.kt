package eu.rigeldev.kuberig.core.execution

interface MethodCallContextProcessor {

    fun process(methodCallContext: MethodCallContext, resources: MutableList<Any>)

}