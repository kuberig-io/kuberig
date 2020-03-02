package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod

/**
 * Abstraction to container resource type instance creation logic.
 */
interface MethodCallContextFactory {

    fun createMethodCallContext(resourceGenerationMethod: ResourceGeneratorMethod, classLoader: ClassLoader): MethodCallContext

}