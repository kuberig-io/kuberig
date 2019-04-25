package eu.rigeldev.kuberig.core.generation

import eu.rigeldev.kuberig.core.generation.yaml.YamlGenerator
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

        for (resourceGeneratorType in resourceGeneratorTypes) {
            println("Found resource generator type: ${resourceGeneratorType.name}")

            val typeClass = dslClasspath.loadClass(resourceGeneratorType.name.replace('/','.'))
            val typeClassInstance = typeClass.getConstructor().newInstance()

            for (resourceGenerationMethod in resourceGeneratorType.resourceGenerationMethods) {
                println(" - $resourceGenerationMethod")

                try {
                    val methodResult = typeClass.getMethod(resourceGenerationMethod).invoke(typeClassInstance)
                    println(methodResult.toString())
                }
                catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}