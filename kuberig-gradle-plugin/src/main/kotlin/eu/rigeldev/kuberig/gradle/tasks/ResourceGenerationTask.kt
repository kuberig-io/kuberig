package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.core.generation.yaml.YamlGenerator
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ResourceGenerationTask : AbstractResourceTask() {

    private var generatedFiles = listOf<File>()

    @TaskAction
    fun generateResources() {
        val generator = YamlGenerator(
            this.environmentFileSystem(),
            this.kubeRigExtension.yamlOutputFileConvention
        )

        val methodResults = this.resourceGeneratorMethodExecutor()
            .execute()

        this.generatedFiles = generator.generate(methodResults)
    }

    @OutputFiles
    fun generatedFiles() : List<File> {
        return this.generatedFiles
    }
}