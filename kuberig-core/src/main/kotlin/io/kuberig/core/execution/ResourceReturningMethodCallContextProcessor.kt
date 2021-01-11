package io.kuberig.core.execution

import io.kuberig.core.resource.RawResourceFactory
import io.kuberig.core.resource.RawResourceInfo
import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource
import io.kuberig.dsl.model.FullResource
import io.kuberig.dsl.support.ApplyActionOverwrite
import io.kuberig.dsl.support.UseDefault
import javassist.ClassPool
import javassist.LoaderClassPath

class ResourceReturningMethodCallContextProcessor(
    private val rawResourceFactory: RawResourceFactory,
    private val classLoader: ClassLoader
) : MethodCallContextProcessor {

    override fun process(
        methodCallContext: MethodCallContext,
        processor: (rawResourceInfo: RawResourceInfo, applyActionOverwrite: ApplyActionOverwrite) -> Unit
    ) {
        val requiredReturnType = KubernetesResourceDslType::class.java
        val actualReturnType = methodCallContext.method.returnType

        check(requiredReturnType.isAssignableFrom(actualReturnType)) { "${methodCallContext.method} returns a $actualReturnType, please correct it to return a $requiredReturnType from within the kinds package." }

        val rawResult = methodCallContext.method.invoke(methodCallContext.typeInstance)

        @Suppress("UNCHECKED_CAST")
        val dslType = rawResult as KubernetesResourceDslType<BasicResource>
        val resource = dslType.toValue()

        val pool = ClassPool.getDefault()
        pool.insertClassPath(LoaderClassPath(classLoader))
        val cc = pool.get(methodCallContext.type.name)
        val methodCc = cc.getDeclaredMethod(methodCallContext.method.name, emptyArray())

        val sourceLocation =
            methodCallContext.type.name + "." + methodCallContext.method.name + "(" + methodCallContext.type.simpleName + ".kt:" + methodCc.methodInfo.getLineNumber(
                0
            ) + ")"

        if (ResourceValidator.isValidResource(dslType, resource, sourceLocation)) {
            val rawResourceInfo = rawResourceFactory.rawResourceInfo(resource as FullResource, sourceLocation)

            processor.invoke(rawResourceInfo, UseDefault)
        }
    }
}