package io.kuberig.core.execution

import io.kuberig.core.preparation.InitialResourceInfo
import io.kuberig.dsl.support.ApplyActionOverwrite
import javassist.ClassPool
import javassist.LoaderClassPath

abstract class AbstractReturningMethodCallContextProcessor<T: Any>(
    private val classLoader: ClassLoader
) : MethodCallContextProcessor {

    override fun process(
        methodCallContext: MethodCallContext,
        processor: (initialResourceInfo: InitialResourceInfo, applyActionOverwrite: ApplyActionOverwrite) -> Unit
    ) {
        val requiredReturnType = requiredReturnType()::class.java
        val actualReturnType = methodCallContext.method.returnType

        check(requiredReturnType.isAssignableFrom(actualReturnType)) { "${methodCallContext.method} returns a $actualReturnType, please correct it to return a $requiredReturnType from within the kinds package." }

        val rawResult = methodCallContext.method.invoke(methodCallContext.typeInstance)

        @Suppress("UNCHECKED_CAST")
        val methodReturnValue = rawResult as T

        val pool = ClassPool.getDefault()
        pool.insertClassPath(LoaderClassPath(classLoader))
        val cc = pool.get(methodCallContext.type.name)
        val methodCc = cc.getDeclaredMethod(methodCallContext.method.name, emptyArray())

        val sourceLocation =
            methodCallContext.type.name + "." + methodCallContext.method.name +
                    "(" + methodCallContext.type.simpleName + ".kt:" + methodCc.methodInfo.getLineNumber(0) + ")"

        process(
            methodReturnValue,
            sourceLocation,
            processor
        )
    }

    protected abstract fun requiredReturnType(): Class<T>

    protected abstract fun process(
        methodReturnValue: T,
        sourceLocation: String,
        processor: (initialResourceInfo: InitialResourceInfo, applyActionOverwrite: ApplyActionOverwrite) -> Unit
    )
}