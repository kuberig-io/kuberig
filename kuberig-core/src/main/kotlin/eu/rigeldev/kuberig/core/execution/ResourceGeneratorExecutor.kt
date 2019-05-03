package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.generation.yaml.YamlGenerator
import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod
import eu.rigeldev.kuberig.dsl.DslType
import java.io.File

class ResourceGeneratorExecutor(val resourceGenerationRuntimeClasspathClassLoader: ClassLoader,
                                val environment: KubeRigEnvironment) {

    fun execute(resourceGeneratorMethod: ResourceGeneratorMethod): ResourceGeneratorMethodResult {
        ResourceGeneratorContext.fill(environment)
        try {
            val type = resourceGenerationRuntimeClasspathClassLoader.loadClass(resourceGeneratorMethod.generatorType)

            val typeInstance = type.getConstructor().newInstance()

            println(" - ${resourceGeneratorMethod.methodName}")

            val dslType = type.getMethod(resourceGeneratorMethod.methodName).invoke(typeInstance) as DslType<Any>

            val resource = dslType.toValue()


            return ResourceGeneratorMethodResult(resourceGeneratorMethod, resource)
        }
        finally {
            ResourceGeneratorContext.clear()
        }
    }

}