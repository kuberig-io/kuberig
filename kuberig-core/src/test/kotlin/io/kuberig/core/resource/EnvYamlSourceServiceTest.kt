package io.kuberig.core.resource

import io.kuberig.core.preparation.InitialResourceInfoFactory
import io.kuberig.dsl.support.yaml.envYamlSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class EnvYamlSourceServiceTest {

    @Test
    fun importEnvYamlSource() {
        val expectedNamespace = "test-default-ns"

        val envYamlSourceService = EnvYamlSourceService(
            InitialResourceInfoFactory(),
            File("testbed/yaml-import-support")
        )

        val envYamlSource = envYamlSource {
            from("src/main/yaml/kind-nginx-ingress-deploy.yaml")
        }
        val result = envYamlSourceService.importEnvYamlSource(
            envYamlSource.toValue()
        )

        assertEquals(18, result.size)
    }
}