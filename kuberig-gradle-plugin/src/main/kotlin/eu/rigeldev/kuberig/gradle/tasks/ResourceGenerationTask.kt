package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.core.generation.yaml.YamlGenerator
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

open class ResourceGenerationTask : AbstractResourceTask() {

    private var generatedFiles = listOf<File>()

    private var groupName: String = ""
    private var allGroups: Boolean = false

    fun getGroupName(): String? {
        return this.groupName
    }

    @Option(option = "group", description = "The resource group to deploy. (optional)")
    fun setGroupName(groupName: String) {
        this.groupName = groupName
    }

    fun getAllGroups(): Boolean {
        return this.allGroups
    }

    @Option(option = "allGroups", description = "Trigger deployment for all resource groups.")
    fun setAllGroups(allGroups: Boolean) {
        this.allGroups = allGroups
    }

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

    fun getGeneratedFiles() : List<File> {
        return this.generatedFiles
    }
}