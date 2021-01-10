package io.kuberig.core.execution

import io.kuberig.core.model.GeneratorMethodAndType

/**
 * The default resource generation method call context factory uses reflection to call the default no args-constructor.
 */
class DefaultMethodCallContextFactory : MethodCallContextFactory {

    override fun createMethodCallContext(generatorMethod: GeneratorMethodAndType, classLoader: ClassLoader): MethodCallContext {
        val type : Class<*> = classLoader.loadClass(generatorMethod.typeName)
        val typeInstance = type.getConstructor().newInstance()
        val method = type.getMethod(generatorMethod.methodName)

        return MethodCallContext(
            type,
            typeInstance,
            method,
            generatorMethod.methodType
        )
    }
}