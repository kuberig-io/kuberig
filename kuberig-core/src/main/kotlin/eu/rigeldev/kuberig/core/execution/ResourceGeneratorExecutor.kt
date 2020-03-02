package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.annotations.Tick
import eu.rigeldev.kuberig.core.detection.EnvResourceAnnotationDetectionListener
import eu.rigeldev.kuberig.core.detection.EnvResourceAnnotationDetector
import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod
import eu.rigeldev.kuberig.dsl.support.DslResourceEmitter
import eu.rigeldev.kuberig.fs.EnvironmentFileSystem
import org.slf4j.LoggerFactory
import java.io.File

class ResourceGeneratorExecutor(
    private val compileOutputDirectory: File,
    private val compileClassPath: Set<File>,
    private val resourceGenerationRuntimeClasspathClassLoader: ClassLoader,
    private val environment: KubeRigEnvironment,
    private val environmentFileSystem: EnvironmentFileSystem,
    private val groupNameMatcher: ResourceGroupNameMatcher
) {

    private val logger = LoggerFactory.getLogger(ResourceGeneratorExecutor::class.java)

    private val methodCallContextProcessors = mapOf(
        Pair(ResourceGenerationMethodType.RESOURCE_RETURNING, ResourceReturningMethodCallContextProcessor()),
        Pair(ResourceGenerationMethodType.RESOURCE_EMITTING, ResourceEmittingMethodCallContextProcessor())
    )

    private val filterDelegate = DelegatingResourceGeneratorFilter(
        listOf(
            EnvResourceGeneratorFilter(environment),
            GroupResourceGenerationFilter(groupNameMatcher)
        )
    )

    private val methodCallContextFactory = DefaultMethodCallContextFactory()

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

    private fun execute(resourceGeneratorMethod: ResourceGeneratorMethod): ResourceGeneratorMethodResult {
        ResourceGeneratorContext.fill(
            environment,
            environmentFileSystem.environmentDirectory,
            environmentFileSystem.loadConfigs(),
            environmentFileSystem.encryptionSupport(),
            environmentFileSystem.rootFileSystem
        )

        DslResourceEmitter.init()

        try {
            val methodCallContext = methodCallContextFactory.createMethodCallContext(resourceGeneratorMethod, resourceGenerationRuntimeClasspathClassLoader)

            if (filterDelegate.shouldGenerate(methodCallContext.method)) {
                val resources = mutableListOf<Any>()

                try {
                    methodCallContextProcessors.getValue(methodCallContext.methodType)
                        .process(methodCallContext, resources)

                } catch (t: Throwable) {
                    return ErrorResult(resourceGeneratorMethod, t.cause ?: t)
                }

                if (resources.isEmpty()) {
                    logger.warn("${resourceGeneratorMethod.generatorType}.${resourceGeneratorMethod.methodName} did not emit any resources!")
                }

                return SuccessResult(
                    resourceGeneratorMethod,
                    resources,
                    extractTick(methodCallContext)
                )
            } else {
                return SkippedResult(resourceGeneratorMethod)
            }
        } finally {
            ResourceGeneratorContext.clear()
        }
    }

    private fun extractTick(methodCallContext: MethodCallContext): Int {
        val tickAnnotation = methodCallContext.method.getDeclaredAnnotation(Tick::class.java)
        return tickAnnotation?.tick ?: 1
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

}