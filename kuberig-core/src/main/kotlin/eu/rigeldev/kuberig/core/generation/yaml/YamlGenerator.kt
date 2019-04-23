package eu.rigeldev.kuberig.core.generation.yaml

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import java.io.File

/**
 * Contains the correct Jackson settings to produce clean yaml output.
 *
 * Note: These are `my` ideal settings, in the future this should be made configurable.
 */
class YamlGenerator {

    private val objectMapper : ObjectMapper

    init {
        val yamlFactory = YAMLFactory()
        yamlFactory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)

        this.objectMapper = ObjectMapper(yamlFactory)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
    }

    fun generateYaml(resource: Any) : String {
        return this.objectMapper.writeValueAsString(resource)
    }

    fun generateYaml(resultFile: File, resource: Any) {
        this.objectMapper.writeValue(resultFile, resource)
    }

}