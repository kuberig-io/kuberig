package eu.rigeldev.kuberig.core.generation.yaml

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorMethodResult
import java.io.File

/**
 * Contains the correct Jackson settings to produce clean yaml output.
 *
 * Note: These are `my` ideal settings, in the future this should be made configurable.
 */
class YamlGenerator(val outputDirectory : File) {

    private val objectMapper : ObjectMapper

    init {
        val yamlFactory = YAMLFactory()
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)

        this.objectMapper = ObjectMapper(yamlFactory)
        this.objectMapper.findAndRegisterModules()
        val byteArrayModule = SimpleModule()
        byteArrayModule.addSerializer(ByteArray::class.java, ByteArraySerializer())
        byteArrayModule.addDeserializer(ByteArray::class.java, ByteArrayDeserializer())
        objectMapper.registerModule(byteArrayModule)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

        if (outputDirectory.exists()) {
            outputDirectory.deleteRecursively()
        }
        outputDirectory.mkdirs()

        println("Generating YAML resources into output directory: $outputDirectory")
    }

    fun generateYaml(resource: Any) : String {
        return this.objectMapper.writeValueAsString(resource)
    }

    fun generateYaml(resultFile: File, resource: Any) {
        this.objectMapper.writeValue(resultFile, resource)
    }

    fun generate(methodResult : ResourceGeneratorMethodResult) : File {
        val yaml = this.generateYaml(methodResult.resource)

        val outputFile = File(outputDirectory, "${methodResult.method.methodName}.yaml")

        outputFile.writeText(yaml)

        return outputFile
    }

}