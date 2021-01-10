package io.kuberig.core.execution

import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource
import io.kuberig.dsl.model.FullResource
import io.kuberig.dsl.support.ApplyActionOverwrite
import io.kuberig.dsl.support.UseDefault
import javassist.ClassPool
import javassist.LoaderClassPath

class ResourceReturningMethodCallContextProcessor(val classLoader: ClassLoader) : MethodCallContextProcessor {

    override fun process(
        methodCallContext: MethodCallContext,
        processor: (resource: FullResource, applyActionOverwrite: ApplyActionOverwrite) -> Unit) {
        val requiredReturnType = KubernetesResourceDslType::class.java
        val actualReturnType = methodCallContext.method.returnType

        check(requiredReturnType.isAssignableFrom(actualReturnType)) { "${methodCallContext.method} returns a $actualReturnType, please correct it to return a $requiredReturnType from within the kinds package." }

        val rawResult = methodCallContext.method.invoke(methodCallContext.typeInstance)

        @Suppress("UNCHECKED_CAST")
        val dslType = rawResult as KubernetesResourceDslType<BasicResource>
        val resource = dslType.toValue()

        val userCallLocationProvider = {
            val pool = ClassPool.getDefault()
            pool.insertClassPath(LoaderClassPath(classLoader))
            val cc = pool.get(methodCallContext.type.name)
            val methodCc = cc.getDeclaredMethod(methodCallContext.method.name, emptyArray())

            methodCallContext.type.name + "." + methodCallContext.method.name + "(" + methodCallContext.type.simpleName + ".kt:" + methodCc.methodInfo.getLineNumber(0) + ")"
        }

        if (ResourceValidator.isValidResource(dslType, resource, userCallLocationProvider)) {
            processor.invoke(resource as FullResource, UseDefault)
        }
    }
}