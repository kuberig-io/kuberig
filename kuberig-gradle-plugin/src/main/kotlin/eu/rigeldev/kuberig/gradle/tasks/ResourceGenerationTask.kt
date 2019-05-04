package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.core.generation.yaml.YamlGenerator
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ResourceGenerationTask : AbstractResourceTask() {

    private var generatedFiles = listOf<File>()

    @TaskAction
    fun generateResources() {
        val executor = this.resourceGeneratorMethodExecutor()

        val generator = YamlGenerator(
            project.file("build/generated-yaml/${environment.name}")
        )

        this.generatedFiles = super.detectResourceGeneratorMethods()
            .map(executor::execute)
            .map(generator::generate)
    }

    @OutputFiles
    fun generatedFiles() : List<File> {
        return this.generatedFiles
    }
}