package eu.rigeldev.kuberig.core.generation

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import eu.rigeldev.kuberig.core.generation.yaml.YamlGenerator
import eu.rigeldev.kuberig.dsl.DslType
import java.io.File
import java.net.URLClassLoader
import java.util.*

class ResourceGenerator {

    private val yamlGenerator = YamlGenerator()

    fun generateResources(resourceGeneratorTypes : List<ResourceGeneratorType>,
                          resourceGenerationClasspath : List<File>,
                          outputDirectory : File) {

        if (outputDirectory.exists()) {
            outputDirectory.deleteRecursively()
        }
        outputDirectory.mkdirs()

        println("Generating YAML resources into output directory: $outputDirectory")

        println("Found # " + resourceGeneratorTypes.size + " resource methods")

        val actualClassPathEntries = resourceGenerationClasspath
            .filter { !it.name.contains("kuberig-annotations") }

        val dslClasspath = URLClassLoader(
            actualClassPathEntries.map {it.toURI().toURL()}.toTypedArray(),
            Thread.currentThread().contextClassLoader
        )

        val objectMapper = ObjectMapper()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

        for (resourceGeneratorType in resourceGeneratorTypes) {
            println("Found resource generator type: ${resourceGeneratorType.name}")

            val typeClass = dslClasspath.loadClass(resourceGeneratorType.name.replace('/','.'))
            val typeClassInstance = typeClass.getConstructor().newInstance()

            for (resourceGenerationMethod in resourceGeneratorType.resourceGenerationMethods) {
                println(" - $resourceGenerationMethod")

                try {
                    val methodResult = typeClass.getMethod(resourceGenerationMethod).invoke(typeClassInstance) as DslType<*>

                    val valueJson = objectMapper.writeValueAsString(methodResult.toValue())

                    val jsonNode = objectMapper.readTree(valueJson)

                    val apiVersion = jsonNode.get("apiVersion").textValue()
                    val kind = jsonNode.get("kind").textValue()
                    val name = jsonNode.get("metadata").get("name").textValue()

                    println("$name - $kind - $apiVersion")
                }
                catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}