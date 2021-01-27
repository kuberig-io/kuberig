package io.kuberig.core.generation.yaml

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import io.kuberig.core.model.SuccessResult
import io.kuberig.core.preparation.ResourcePreparation
import io.kuberig.core.resource.RawJsonResourceInfo
import io.kuberig.fs.EnvironmentFileSystem
import io.kuberig.fs.OutputFileConvention
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Contains the correct Jackson settings to produce clean yaml output.
 *
 * Note: These are `my` ideal settings, in the future this should be made configurable.
 */
class YamlGenerator(
    private val environmentFileSystem: EnvironmentFileSystem,
    private val outputFileConvention: OutputFileConvention,
    private val resourcePreparation: ResourcePreparation
) {

    private val logger = LoggerFactory.getLogger(YamlGenerator::class.java)

    private val objectMapper : ObjectMapper
    private val outputDirectory : File

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

        this.environmentFileSystem.clearGeneratedYamlDirectory()
        this.environmentFileSystem.createGeneratedYamlDirectory()

        this.outputDirectory = this.environmentFileSystem.generatedYamlDirectory()

        logger.info("Generating YAML resources into output directory: $outputDirectory")
    }

    private fun generateYaml(rawJsonResourceInfo: RawJsonResourceInfo) : String {
        return this.objectMapper.writeValueAsString(rawJsonResourceInfo.json)
    }

    fun generate(methodResults: List<SuccessResult>) : List<File> {
        val generatedFiles = mutableListOf<File>()

        for (methodResult in methodResults) {
            for (resourceApplyRequest in methodResult.resourceApplyRequests) {
                val rawJsonResourceInfo = resourcePreparation.prepare(resourceApplyRequest.initialResourceInfo)

                val yaml = this.generateYaml(rawJsonResourceInfo)

                val outputFile = this.outputFileConvention.outputFile(this.outputDirectory, yaml)

                outputFile.writeText(yaml)

                generatedFiles.add(outputFile)
            }
        }

        return generatedFiles
    }

}