package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.annotations.EnvFilter
import eu.rigeldev.kuberig.core.annotations.EnvResource
import eu.rigeldev.kuberig.core.annotations.Tick
import eu.rigeldev.kuberig.core.detection.EnvResourceAnnotationDetectionListener
import eu.rigeldev.kuberig.core.detection.EnvResourceAnnotationDetector
import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod
import eu.rigeldev.kuberig.dsl.DslType
import eu.rigeldev.kuberig.dsl.support.DslResourceEmitter
import eu.rigeldev.kuberig.dsl.support.DslResourceReceiver
import eu.rigeldev.kuberig.fs.EnvironmentFileSystem
import org.slf4j.LoggerFactory
import java.io.File

class ResourceGeneratorExecutor(
    private val compileOutputDirectory: File,
    private val compileClassPath: Set<File>,
    private val resourceGenerationRuntimeClasspathClassLoader: ClassLoader,
    private val environment: KubeRigEnvironment,
    private val environmentFileSystem: EnvironmentFileSystem
) {

    private val logger = LoggerFactory.getLogger(ResourceGeneratorExecutor::class.java)

    fun execute(): List<ResourceGeneratorMethodResult> {
        val resourceGeneratorMethods = mutableListOf<ResourceGeneratorMethod>()

        val detector = EnvResourceAnnotationDetector(
            listOf(compileOutputDirectory),
            compileClassPath,
            object : EnvResourceAnnotationDetectionListener {
                override fun receiveEnvResourceAnnotatedType(className: String, annotatedMethods: Set<String>) {
                    annotatedMethods.forEach { annotatedMethod ->
                        resourceGeneratorMethods.add(ResourceGeneratorMethod(className, annotatedMethod))
                    }
                }
            }
        )

        detector.scanClassesDirectories()

        val methodResults = resourceGeneratorMethods.map(this::execute)

        reportAndFailOnErrors(methodResults)

        return methodResults
    }

    private fun reportAndFailOnErrors(methodResults: List<ResourceGeneratorMethodResult>) {
        val errorResults: List<ErrorResult> = methodResults
            .filter { it.javaClass == ErrorResult::class.java }
            .map { it as ErrorResult }

        if (errorResults.isNotEmpty()) {
            errorResults.forEach {
                logger.error("Error during execution of ${it.method.generatorType}.${it.method.methodName}: ${it.errorMessage()}")
            }

            throw IllegalStateException("Not all resource generation methods were executed successfully.")
        }
    }

    fun execute(resourceGeneratorMethod: ResourceGeneratorMethod): ResourceGeneratorMethodResult {
        val environmentDirectory = this.environmentFileSystem.environmentDirectory

        val environmentConfigs = this.environmentFileSystem.loadConfigs()
        val environmentEncryptionSupport = this.environmentFileSystem.encryptionSupport()

        ResourceGeneratorContext.fill(
            environment,
            environmentDirectory,
            environmentConfigs,
            environmentEncryptionSupport,
            environmentFileSystem.rootFileSystem
        )

        val resources = mutableListOf<Any>()

        DslResourceEmitter.init()

        try {
            val type = resourceGenerationRuntimeClasspathClassLoader.loadClass(resourceGeneratorMethod.generatorType)

            val typeInstance = type.getConstructor().newInstance()

            val method = type.getMethod(resourceGeneratorMethod.methodName)

            val singleResourceMethod = method.getDeclaredAnnotation(EnvResource::class.java) != null

            if (!singleResourceMethod) {
                DslResourceEmitter.registerReceiver(object : DslResourceReceiver {
                    override fun getName(): String {
                        return "default-receiver"
                    }

                    override fun <T> receive(dslType: DslType<T>) {
                        resources.add(dslType.toValue() as Any)
                    }
                })
            }

            val envFilterAnnotation = method.getDeclaredAnnotation(EnvFilter::class.java)
            val executionNeeded = envFilterAnnotation?.environments?.map(String::toLowerCase)?.contains(this.environment.name.toLowerCase())
                ?: true

            if (executionNeeded) {
                val tickAnnotation = method.getDeclaredAnnotation(Tick::class.java)
                val tick = tickAnnotation?.tick ?: 1

                try {
                    if (singleResourceMethod) {
                        @Suppress("UNCHECKED_CAST") val dslType = method.invoke(typeInstance) as DslType<Any>

                        resources.add(dslType.toValue())
                    } else {
                        method.invoke(typeInstance)
                    }
                } catch (t: Throwable) {
                    return ErrorResult(resourceGeneratorMethod, t.cause ?: t)
                }

                if (resources.isEmpty()) {
                    logger.warn("${resourceGeneratorMethod.generatorType}.${resourceGeneratorMethod.methodName} did not emit any resources!")
                }

                return SuccessResult(
                    resourceGeneratorMethod,
                    resources,
                    tick
                )
            } else {
                return SkippedResult(resourceGeneratorMethod)
            }


        } finally {
            ResourceGeneratorContext.clear()
        }
    }

}