package io.kuberig.core

import io.kuberig.core.execution.ResourceGeneratorExecutor
import io.kuberig.core.generation.yaml.YamlGenerator
import io.kuberig.core.preparation.InitialResourceInfoFactory
import io.kuberig.core.preparation.ResourcePreparation
import io.kuberig.core.resource.EnvYamlSourceService
import io.kuberig.fs.OutputFileConvention

class GenerateYaml : HighLevelResourceAction<GenerateYaml>() {

    private lateinit var yamlOutputFileConvention: OutputFileConvention

    fun yamlOutputFileConvention(yamlOutputFileConvention: OutputFileConvention): GenerateYaml {
        this.yamlOutputFileConvention = yamlOutputFileConvention

        return this
    }

    override fun execute() {
        val environmentFileSystem = rootFileSystem.environment(environment.name)

        val initialResourceInfoFactory = InitialResourceInfoFactory()

        val resourceGeneratorExecutor = ResourceGeneratorExecutor(
            compileOutputDirectory,
            resourceGenerationCompileClasspath,
            resourceGenerationRuntimeClasspath,
            environment,
            environmentFileSystem,
            groupNameMatcher,
            initialResourceInfoFactory,
            EnvYamlSourceService(initialResourceInfoFactory, rootFileSystem.repoRootDir)
        )

        val methodResults = resourceGeneratorExecutor.execute()

        val resourcePreparation = ResourcePreparation(environmentFileSystem, flags, methodResults)

        val generator = YamlGenerator(
            environmentFileSystem,
            yamlOutputFileConvention,
            resourcePreparation
        )

        generator.generate(methodResults)
    }

}