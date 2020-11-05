package io.kuberig.core.execution

import io.kuberig.annotations.EnvResource
import io.kuberig.core.detection.ResourceGeneratorMethod

/**
 * The default resource generation method call context factory uses reflection to call the default no args-constructor.
 */
class DefaultMethodCallContextFactory : MethodCallContextFactory {

    override fun createMethodCallContext(resourceGenerationMethod: ResourceGeneratorMethod, classLoader: ClassLoader): MethodCallContext {
        val type : Class<*> = classLoader.loadClass(resourceGenerationMethod.generatorType)
        val typeInstance = type.getConstructor().newInstance()
        val method = type.getMethod(resourceGenerationMethod.methodName)

        val methodType = if (method.getDeclaredAnnotation(EnvResource::class.java) != null) {
            ResourceGenerationMethodType.RESOURCE_RETURNING
        } else {
            ResourceGenerationMethodType.RESOURCE_EMITTING
        }

        return MethodCallContext(type, typeInstance, method, methodType)
    }
}