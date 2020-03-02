package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.dsl.DslType

class ResourceReturningMethodCallContextProcessor : MethodCallContextProcessor {
    override fun process(methodCallContext: MethodCallContext, resources: MutableList<Any>) {
        val requiredReturnType = DslType::class.java
        val actualReturnType = methodCallContext.method.returnType

        check(requiredReturnType.isAssignableFrom(actualReturnType)) { "${methodCallContext.method} returns a $actualReturnType, please correct it to return a $requiredReturnType from within the kinds package." }

        @Suppress("UNCHECKED_CAST") val dslType = methodCallContext.method.invoke(methodCallContext.typeInstance) as DslType<Any>

        resources.add(dslType.toValue())
    }
}