package io.kuberig.core.execution

import io.kuberig.core.detection.ResourceGeneratorMethod

/**
 * Abstraction to container resource type instance creation logic.
 */
interface MethodCallContextFactory {

    fun createMethodCallContext(resourceGenerationMethod: ResourceGeneratorMethod, classLoader: ClassLoader): MethodCallContext

}