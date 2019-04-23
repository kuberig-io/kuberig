package eu.rigeldev.kuberig.core.generation

import eu.rigeldev.kuberig.core.generation.yaml.YamlGenerator
import java.io.File

class ResourceGenerator {

    private val yamlGenerator = YamlGenerator()

    fun generateResources(resourceGeneratorTypes : List<ResourceGeneratorType>, outputDirectory : File) {

        if (outputDirectory.exists()) {
            outputDirectory.deleteRecursively()
        }
        outputDirectory.mkdirs()

        println("Generating YAML resources into output directory: $outputDirectory")

        println("Found # " + resourceGeneratorTypes.size + " resource methods")

        for (resourceGeneratorType in resourceGeneratorTypes) {
            println("Found resource generator type: ${resourceGeneratorType.name}")

            for (resourceGenerationMethod in resourceGeneratorType.resourceGenerationMethods) {
                println(" - $resourceGenerationMethod")
            }
        }
    }

}