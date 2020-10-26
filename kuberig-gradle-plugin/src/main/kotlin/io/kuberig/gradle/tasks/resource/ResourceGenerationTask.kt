package io.kuberig.gradle.tasks.resource

import io.kuberig.core.generation.yaml.YamlGenerator
import io.kuberig.gradle.tasks.AbstractResourceTask
import org.gradle.api.tasks.TaskAction

open class ResourceGenerationTask : AbstractResourceTask() {

    @TaskAction
    fun generateResources() {
        val generator = YamlGenerator(
            this.environmentFileSystem(),
            this.kubeRigExtension().yamlOutputFileConvention
        )

        val groupNameMatcher = this.groupNameMatcher(groupName, allGroups)
        val methodResults = this.resourceGeneratorMethodExecutor(groupNameMatcher)
            .execute()

        generator.generate(methodResults)
    }

}