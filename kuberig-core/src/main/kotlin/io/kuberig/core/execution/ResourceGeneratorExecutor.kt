package io.kuberig.core.execution

import io.kuberig.annotations.*
import io.kuberig.config.KubeRigEnvironment
import io.kuberig.core.detection.EnvResourceAnnotationDetector
import io.kuberig.core.detection.GeneratorTypeConsumer
import io.kuberig.core.execution.filtering.DelegatingResourceGeneratorFilter
import io.kuberig.core.execution.filtering.environment.EnvResourceGeneratorFilter
import io.kuberig.core.execution.filtering.group.GroupResourceGenerationFilter
import io.kuberig.core.execution.filtering.group.ResourceGroupNameMatcher
import io.kuberig.core.model.*
import io.kuberig.core.preparation.InitialResourceInfoFactory
import io.kuberig.core.resource.EnvYamlSourceService
import io.kuberig.dsl.support.DslResourceEmitter
import io.kuberig.dsl.support.UseDefault
import io.kuberig.dsl.support.UseOverwrite
import io.kuberig.fs.EnvironmentFileSystem
import org.slf4j.LoggerFactory
import java.io.File

class ResourceGeneratorExecutor(
    private val compileOutputDirectory: File,
    private val compileClassPath: Set<File>,
    private val resourceGenerationRuntimeClasspathClassLoader: ClassLoader,
    private val environment: KubeRigEnvironment,
    private val environmentFileSystem: EnvironmentFileSystem,
    groupNameMatcher: ResourceGroupNameMatcher,
    initialResourceInfoFactory: InitialResourceInfoFactory,
    envYamlSourceService: EnvYamlSourceService
) {

    private val logger = LoggerFactory.getLogger(ResourceGeneratorExecutor::class.java)

    private val methodCallContextProcessors = mapOf(
        Pair(
            GeneratorMethodType.RESOURCE_RETURNING,
            ResourceReturningMethodCallContextProcessor(initialResourceInfoFactory, resourceGenerationRuntimeClasspathClassLoader)
        ),
        Pair(GeneratorMethodType.RESOURCE_EMITTING, ResourceEmittingMethodCallContextProcessor(initialResourceInfoFactory, envYamlSourceService)),
        Pair(GeneratorMethodType.RAW_YAML, RawYamlMethodCallContextProcessor(envYamlSourceService, resourceGenerationRuntimeClasspathClassLoader))
    )

    private val filterDelegate = DelegatingResourceGeneratorFilter(
        listOf(
            EnvResourceGeneratorFilter(environment),
            GroupResourceGenerationFilter(groupNameMatcher)
        )
    )

    private val methodCallContextFactory = DefaultMethodCallContextFactory()

    fun execute(): List<SuccessResult> {
        val generatorMethodResults = mutableListOf<GeneratorMethodResult>()

        val detector = EnvResourceAnnotationDetector(
            listOf(compileOutputDirectory),
            compileClassPath,
            object : GeneratorTypeConsumer {
                override fun consume(generatorType: GeneratorType) {
                    for (generatorMethod in generatorType.generatorMethods) {
                        val generatorMethodAndType = GeneratorMethodAndType(
                            generatorType.typeName,
                            generatorMethod.generatorMethodType,
                            generatorMethod.methodName
                        )

                        val generatorMethodResult = execute(generatorMethodAndType)

                        generatorMethodResults.add(generatorMethodResult)

                    }
                }
            }
        )

        detector.scanClassesDirectories()

        reportAndFailOnErrors(generatorMethodResults)

        return generatorMethodResults.filterIsInstance(SuccessResult::class.java)
    }

    private fun execute(generatorMethod: GeneratorMethodAndType): GeneratorMethodResult {
        ResourceGeneratorContext.fill(
            environment,
            environmentFileSystem.environmentDirectory,
            environmentFileSystem.loadConfigs(),
            environmentFileSystem.encryptionSupport(),
            environmentFileSystem.rootFileSystem
        )

        DslResourceEmitter.init()

        try {
            val methodCallContext = methodCallContextFactory.createMethodCallContext(
                generatorMethod,
                resourceGenerationRuntimeClasspathClassLoader
            )

            if (filterDelegate.shouldGenerate(methodCallContext.method)) {
                val resources = mutableListOf<ResourceApplyRequest>()

                val tick = extractTick(methodCallContext)
                val defaultApplyAction = extractApplyAction(methodCallContext)

                try {
                    val processor = methodCallContextProcessors.getValue(methodCallContext.methodType)

                    processor.process(methodCallContext) { initialResourceInfo, applyActionOverwrite ->
                        val applyAction = when(applyActionOverwrite) {
                            is UseDefault -> defaultApplyAction
                            is UseOverwrite -> applyActionOverwrite.action
                        }

                        resources.add(
                            ResourceApplyRequest(
                                initialResourceInfo,
                                applyAction,
                                tick
                            )
                        )
                    }

                } catch (t: Throwable) {
                    logger.error("Failed to execute resource generation method", t)
                    return ErrorResult(generatorMethod, t.cause ?: t)
                }

                if (resources.isEmpty()) {
                    logger.warn("${generatorMethod.fullMethod()} did not emit any resources!")
                }

                return SuccessResult(generatorMethod, resources)
            } else {
                return SkippedResult(generatorMethod)
            }
        } finally {
            ResourceGeneratorContext.clear()
        }
    }

    private fun extractTick(methodCallContext: MethodCallContext): Int {
        val tickAnnotation = methodCallContext.method.getDeclaredAnnotation(Tick::class.java)
        return tickAnnotation?.tick ?: 1
    }

    private fun extractApplyAction(methodCallContext: MethodCallContext): ApplyAction {
        return when(methodCallContext.methodType) {
            GeneratorMethodType.RESOURCE_EMITTING -> {
                methodCallContext.method.getDeclaredAnnotation(EnvResources::class.java).defaultAction
            }
            GeneratorMethodType.RESOURCE_RETURNING -> {
                methodCallContext.method.getDeclaredAnnotation(EnvResource::class.java).action
            }
            GeneratorMethodType.RAW_YAML -> {
                methodCallContext.method.getDeclaredAnnotation(EnvYaml::class.java).action
            }
        }
    }

    private fun reportAndFailOnErrors(methodResults: List<GeneratorMethodResult>) {
        val errorResults: List<ErrorResult> = methodResults
            .filter { it.javaClass == ErrorResult::class.java }
            .map { it as ErrorResult }

        if (errorResults.isNotEmpty()) {
            errorResults.forEach {
                logger.error("Error during execution of ${it.fullMethodName()}:", it.rootCause)
            }

            throw IllegalStateException("Not all resource generation methods were executed successfully.")
        }
    }

}