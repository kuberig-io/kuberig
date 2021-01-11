package io.kuberig.core.resource

import io.kuberig.dsl.support.yaml.EnvYamlSource
import io.kuberig.fs.RootFileSystem

/**
 * YAML file to RawResourceInfo conversion service.
 */
class EnvYamlSourceService(
    private val rawResourceFactory: RawResourceFactory,
    private val rootFileSystem: RootFileSystem
) {

    fun importEnvYamlSource(envYamlSource: EnvYamlSource): List<RawResourceInfo> {
        val rawResourceInfoList = mutableListOf<RawResourceInfo>()

        // TODO #36 read the yaml file and convert the defined resources from YAML to RawResourceInfo

        return rawResourceInfoList
    }

}