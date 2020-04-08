package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.core.generation.yaml.YamlGenerator
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

open class ResourceGenerationTask : AbstractResourceTask() {

    var generatedFiles = listOf<File>()
        @OutputFiles
        get

    @TaskAction
    fun generateResources() {
        val generator = YamlGenerator(
            this.environmentFileSystem(),
            this.kubeRigExtension().yamlOutputFileConvention
        )

        val groupNameMatcher = this.groupNameMatcher(groupName, allGroups)
        val methodResults = this.resourceGeneratorMethodExecutor(groupNameMatcher)
            .execute()

        this.generatedFiles = generator.generate(methodResults)
    }

}