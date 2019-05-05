package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.annotations.EnvFilter
import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod
import eu.rigeldev.kuberig.dsl.DslType
import java.io.File
import java.util.*

class ResourceGeneratorExecutor(private val projectDirectory: File,
                                private val resourceGenerationRuntimeClasspathClassLoader: ClassLoader,
                                private val environment: KubeRigEnvironment) {

    fun execute(resourceGeneratorMethod: ResourceGeneratorMethod): ResourceGeneratorMethodResult {
        val environmentsDirectory = File(this.projectDirectory, "environments")
        val globalConfigsFile = File(environmentsDirectory, "global-configs.properties")
        val environmentDirectory = File(environmentsDirectory, environment.name)
        val environmentConfigsFile = File(environmentDirectory, "${environment.name}-configs.properties")

        val environmentConfigs = loadProperties(environmentConfigsFile)
        val globalConfigs = loadProperties(globalConfigsFile)
        ResourceGeneratorContext.fill(
            environment,
            projectDirectory,
            environmentDirectory,
            environmentConfigs,
            globalConfigs
        )
        try {
            val type = resourceGenerationRuntimeClasspathClassLoader.loadClass(resourceGeneratorMethod.generatorType)

            val typeInstance = type.getConstructor().newInstance()

            val method = type.getMethod(resourceGeneratorMethod.methodName)

            val envFilterAnnotation = method.getDeclaredAnnotation(EnvFilter::class.java)
            val executionNeeded = if (envFilterAnnotation != null) {
                  (envFilterAnnotation as EnvFilter).environments
                     .map(String::toLowerCase)
                    .contains(this.environment.name.toLowerCase())
            } else {
                true
            }

            return if (executionNeeded) {
                try {
                    @Suppress("UNCHECKED_CAST") val dslType = method.invoke(typeInstance) as DslType<Any>

                    val resource = dslType.toValue()

                    SuccessResult(resourceGeneratorMethod, resource)
                }
                catch(t : Throwable) {
                    ErrorResult(resourceGeneratorMethod, t)
                }

            } else {
                SkippedResult(resourceGeneratorMethod)
            }


        }
        finally {
            ResourceGeneratorContext.clear()
        }
    }

    private fun loadProperties(propertiesFile: File) : Properties {
        val properties = Properties()

        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use {
                properties.load(it)
            }
        }

        return properties
    }

}