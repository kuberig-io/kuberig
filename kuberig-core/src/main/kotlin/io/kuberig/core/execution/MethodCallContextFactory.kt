package io.kuberig.core.execution

import io.kuberig.core.model.GeneratorMethodAndType

/**
 * Abstraction to container resource type instance creation logic.
 */
interface MethodCallContextFactory {

    fun createMethodCallContext(generatorMethod: GeneratorMethodAndType, classLoader: ClassLoader): MethodCallContext

}