package io.kuberig.core.execution

import io.kuberig.core.preparation.InitialResourceInfo
import io.kuberig.core.resource.EnvYamlSourceService
import io.kuberig.dsl.support.ApplyActionOverwrite
import io.kuberig.dsl.support.UseDefault
import io.kuberig.dsl.support.yaml.EnvYamlSourceDsl

class RawYamlMethodCallContextProcessor(
    private val envYamlSourceService: EnvYamlSourceService,
    classLoader: ClassLoader
) : AbstractReturningMethodCallContextProcessor<EnvYamlSourceDsl>(classLoader) {

    override fun requiredReturnType(): Class<EnvYamlSourceDsl> {
        return EnvYamlSourceDsl::class.java
    }

    override fun process(
        methodReturnValue: EnvYamlSourceDsl,
        sourceLocation: String,
        processor: (initialResourceInfo: InitialResourceInfo, applyActionOverwrite: ApplyActionOverwrite) -> Unit
    ) {
        val envYamlSource = methodReturnValue.toValue()

        envYamlSourceService.importEnvYamlSource(envYamlSource).forEach {
            processor.invoke(it, UseDefault)
        }
    }
}