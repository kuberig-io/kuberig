package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.dsl.KubernetesResourceDslType
import eu.rigeldev.kuberig.dsl.model.BasicResource
import eu.rigeldev.kuberig.dsl.model.FullResource
import javassist.ClassPool
import javassist.LoaderClassPath

class ResourceReturningMethodCallContextProcessor(val classLoader: ClassLoader) : MethodCallContextProcessor {
    override fun process(methodCallContext: MethodCallContext, resources: MutableList<FullResource>) {
        val requiredReturnType = KubernetesResourceDslType::class.java
        val actualReturnType = methodCallContext.method.returnType

        check(requiredReturnType.isAssignableFrom(actualReturnType)) { "${methodCallContext.method} returns a $actualReturnType, please correct it to return a $requiredReturnType from within the kinds package." }

        @Suppress("UNCHECKED_CAST") val dslType =
            methodCallContext.method.invoke(methodCallContext.typeInstance) as KubernetesResourceDslType<BasicResource>

        val resource = dslType.toValue()

        MethodCallContextProcessorResourceFilter.filteringAdd(
            dslType,
            resource,
            resources,
            {
                val pool = ClassPool.getDefault()
                pool.insertClassPath(LoaderClassPath(classLoader))
                val cc = pool.get(methodCallContext.type.name)
                val methodCc = cc.getDeclaredMethod(methodCallContext.method.name, emptyArray())

                methodCallContext.type.name + "." + methodCallContext.method.name + "(" +methodCallContext.type.simpleName + ".kt:" + methodCc.methodInfo.getLineNumber(0) + ")" }
        )
    }
}