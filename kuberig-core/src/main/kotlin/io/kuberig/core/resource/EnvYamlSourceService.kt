package io.kuberig.core.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.kuberig.core.preparation.InitialResourceInfo
import io.kuberig.core.preparation.InitialResourceInfoFactory
import io.kuberig.dsl.support.yaml.EnvYamlSource
import io.kuberig.dsl.support.yaml.GenericStreamSupplier
import io.kuberig.dsl.support.yaml.RepositoryLocationStreamSupplier
import java.io.File

/**
 * YAML file to RawResourceInfo conversion service.
 */
class EnvYamlSourceService(
    private val initialResourceInfoFactory: InitialResourceInfoFactory,
    private val repoRootDir: File
) {
    private val yamlFactory = YAMLFactory()
    private val yamlObjectMapper = ObjectMapper(YAMLFactory())

    fun importEnvYamlSource(envYamlSource: EnvYamlSource): List<InitialResourceInfo> {
        val inputStream = when(val streamSupplier = envYamlSource.streamSupplier) {
            is RepositoryLocationStreamSupplier -> {
                streamSupplier.streamSupplier.invoke(repoRootDir)
            }
            is GenericStreamSupplier -> {
                streamSupplier.streamSupplier.invoke(envYamlSource.location)
            }
        }

        val yamlParser = yamlFactory.createParser(inputStream)
        val rawYamlResources = yamlObjectMapper
            .readValues<ObjectNode>(yamlParser, object : TypeReference<ObjectNode>() {})
            .readAll()

        val initialResourceInfoList = mutableListOf<InitialResourceInfo>()

        rawYamlResources.withIndex().forEach {
            val objectNode = it.value as ObjectNode

            val initialJsonText = ResourceSerializer.writeValueAsString(objectNode)

            val initialResourceInfo = initialResourceInfoFactory.create(
                initialJsonText,
                envYamlSource.location,
                envYamlSource.targetNamespace
            )

            initialResourceInfoList.add(initialResourceInfo)
        }

        return initialResourceInfoList.toList()
    }

}