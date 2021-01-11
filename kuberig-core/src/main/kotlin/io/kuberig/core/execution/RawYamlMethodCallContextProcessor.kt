package io.kuberig.core.execution

import io.kuberig.core.resource.EnvYamlSourceService
import io.kuberig.core.resource.RawResourceInfo
import io.kuberig.dsl.support.ApplyActionOverwrite

class RawYamlMethodCallContextProcessor(val envYamlSourceService: EnvYamlSourceService) : MethodCallContextProcessor {

    override fun process(
        methodCallContext: MethodCallContext,
        processor: (rawResourceInfo: RawResourceInfo, applyActionOverwrite: ApplyActionOverwrite) -> Unit
    ) {
        // TODO convert a single yaml source into processor.invoke calls.


    }
}