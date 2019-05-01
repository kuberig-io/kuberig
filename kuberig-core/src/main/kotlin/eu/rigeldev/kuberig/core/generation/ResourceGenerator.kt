package eu.rigeldev.kuberig.core.generation

import eu.rigeldev.kuberig.core.generation.yaml.YamlGenerator
import eu.rigeldev.kuberig.dsl.DslType
import java.io.File

class ResourceGenerator {

    private val yamlGenerator = YamlGenerator()

    fun generateResources(
        resourceGeneratorTypes: List<ResourceGeneratorType>,
        outputDirectory: File,
        resourceGenerationRuntimeClasspathClassLoader: ClassLoader
    ) {

        if (outputDirectory.exists()) {
            outputDirectory.deleteRecursively()
        }
        outputDirectory.mkdirs()

        println("Generating YAML resources into output directory: $outputDirectory")

        println("Found # " + resourceGeneratorTypes.size + " resource methods")

        for (resourceGeneratorType in resourceGeneratorTypes) {

            val type = resourceGenerationRuntimeClasspathClassLoader.loadClass(resourceGeneratorType.name.replace('/','.'))

            val typeInstance = type.getConstructor().newInstance()

            println("Found resource generator type: ${resourceGeneratorType.name}")

            for (resourceGenerationMethod in resourceGeneratorType.resourceGenerationMethods) {
                println(" - $resourceGenerationMethod")

                val dslType = type.getMethod(resourceGenerationMethod).invoke(typeInstance) as DslType<Any>

                val resource = dslType.toValue()

                val yaml = yamlGenerator.generateYaml(resource)

                File(outputDirectory, "$resourceGenerationMethod.yaml").writeText(yaml)
            }
        }
    }

}