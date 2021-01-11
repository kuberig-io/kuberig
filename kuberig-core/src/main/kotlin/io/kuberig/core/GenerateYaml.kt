package io.kuberig.core

import io.kuberig.core.execution.ResourceGeneratorExecutor
import io.kuberig.core.generation.yaml.YamlGenerator
import io.kuberig.core.resource.EnvYamlSourceService
import io.kuberig.core.resource.RawResourceFactory
import io.kuberig.fs.OutputFileConvention

class GenerateYaml : HighLevelResourceAction<GenerateYaml>() {

    private lateinit var yamlOutputFileConvention: OutputFileConvention

    fun yamlOutputFileConvention(yamlOutputFileConvention: OutputFileConvention): GenerateYaml {
        this.yamlOutputFileConvention = yamlOutputFileConvention

        return this
    }

    override fun execute() {
        val environmentFileSystem = rootFileSystem.environment(environment.name)

        val rawResourceFactory = RawResourceFactory(environmentFileSystem.defaultNamespace())

        val resourceGeneratorExecutor = ResourceGeneratorExecutor(
            compileOutputDirectory,
            resourceGenerationCompileClasspath,
            resourceGenerationRuntimeClasspath,
            environment,
            environmentFileSystem,
            groupNameMatcher,
            rawResourceFactory,
            EnvYamlSourceService(rawResourceFactory, rootFileSystem)
        )

        val methodResults = resourceGeneratorExecutor.execute()

        val generator = YamlGenerator(
            environmentFileSystem,
            yamlOutputFileConvention
        )

        generator.generate(methodResults)
    }

}