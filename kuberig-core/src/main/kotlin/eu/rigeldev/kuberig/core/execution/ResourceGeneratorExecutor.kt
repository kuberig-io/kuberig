package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.annotations.EnvFilter
import eu.rigeldev.kuberig.core.annotations.Tick
import eu.rigeldev.kuberig.core.detection.ResourceGeneratorDetector
import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod
import eu.rigeldev.kuberig.dsl.DslType
import java.io.File
import java.util.*

class ResourceGeneratorExecutor(private val projectDirectory: File,
                                private val compileOutputDirectory : File,
                                private val resourceGenerationRuntimeClasspathClassLoader: ClassLoader,
                                private val environment: KubeRigEnvironment) {

    fun execute(): List<ResourceGeneratorMethodResult> {
        val detector = ResourceGeneratorDetector(compileOutputDirectory)
        val methods = detector.detectResourceGeneratorMethods()

        val methodResults = methods.map(this::execute)

        reportAndFailOnErrors(methodResults)

        return methodResults
    }

    private fun reportAndFailOnErrors(methodResults : List<ResourceGeneratorMethodResult>) {
        val errorResults : List<ErrorResult> = methodResults
            .filter { it.javaClass == ErrorResult::class.java }
            .map { it as ErrorResult }

        if (errorResults.isNotEmpty()) {
            errorResults.forEach {
                println("[ERROR] ${it.method.generatorType}.${it.method.methodName}: ${it.errorMessage()}")
            }

            throw IllegalStateException("Not all @EnvResource method executions were successful")
        }
    }

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
                  envFilterAnnotation.environments
                     .map(String::toLowerCase)
                    .contains(this.environment.name.toLowerCase())
            } else {
                true
            }

            return if (executionNeeded) {
                try {
                    @Suppress("UNCHECKED_CAST") val dslType = method.invoke(typeInstance) as DslType<Any>

                    val resource = dslType.toValue()

                    val tickAnnotation = method.getDeclaredAnnotation(Tick::class.java)
                    val tick = if (tickAnnotation == null) {
                        1
                    } else {
                        tickAnnotation.tick
                    }

                    SuccessResult(resourceGeneratorMethod, resource, tick)
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