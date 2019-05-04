package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.generation.yaml.YamlGenerator
import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod
import eu.rigeldev.kuberig.dsl.DslType
import java.io.File
import java.util.*

class ResourceGeneratorExecutor(private val projectDirectory: File,
                                private val resourceGenerationRuntimeClasspathClassLoader: ClassLoader,
                                private val environment: KubeRigEnvironment) {

    fun execute(resourceGeneratorMethod: ResourceGeneratorMethod): ResourceGeneratorMethodResult {
        val environmentConfigs = Properties()
        val environmentConfigsFile = File(this.projectDirectory, "environments/" +
                "${environment.name}/" +
                "${environment.name}-environment.properties"
        )
        if (environmentConfigsFile.exists()) {
            environmentConfigsFile.inputStream().use {
                environmentConfigs.load(it)
            }
        } else {
            println("Environment configs properties file ${environmentConfigsFile.absolutePath} is not available.")
        }

        ResourceGeneratorContext.fill(environment, environmentConfigs)
        try {
            val type = resourceGenerationRuntimeClasspathClassLoader.loadClass(resourceGeneratorMethod.generatorType)

            val typeInstance = type.getConstructor().newInstance()

            @Suppress("UNCHECKED_CAST") val dslType = type.getMethod(resourceGeneratorMethod.methodName).invoke(typeInstance) as DslType<Any>

            val resource = dslType.toValue()


            return ResourceGeneratorMethodResult(resourceGeneratorMethod, resource)
        }
        finally {
            ResourceGeneratorContext.clear()
        }
    }

}