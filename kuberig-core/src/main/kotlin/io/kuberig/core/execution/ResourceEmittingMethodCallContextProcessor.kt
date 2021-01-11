package io.kuberig.core.execution

import io.kuberig.core.resource.RawResourceFactory
import io.kuberig.core.resource.RawResourceInfo
import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource
import io.kuberig.dsl.model.FullResource
import io.kuberig.dsl.support.ApplyActionOverwrite
import io.kuberig.dsl.support.DslResourceEmitter
import io.kuberig.dsl.support.DslResourceReceiver
import io.kuberig.dsl.support.yaml.EnvYamlSource

class ResourceEmittingMethodCallContextProcessor(
    private val rawResourceFactory: RawResourceFactory
) : MethodCallContextProcessor {

    override fun process(
        methodCallContext: MethodCallContext,
        processor: (rawResourceInfo: RawResourceInfo, applyActionOverwrite: ApplyActionOverwrite) -> Unit
    ) {

        DslResourceEmitter.registerReceiver(object : DslResourceReceiver {
            override fun getName(): String {
                return "default-receiver"
            }

            override fun <T : BasicResource> receive(
                dslType: KubernetesResourceDslType<T>,
                applyActionOverwrite: ApplyActionOverwrite
            ) {
                val e = IllegalStateException("")

                var userCallLocation = ""
                val stackIterator = e.stackTrace.iterator()

                while (userCallLocation == "" && stackIterator.hasNext()) {
                    val stackTraceElement = stackIterator.next()

                    if (stackTraceElement.className == methodCallContext.type.name) {
                        userCallLocation = stackTraceElement.toString()
                    }
                }

                val sourceLocation = userCallLocation

                val resource = dslType.toValue()

                if (ResourceValidator.isValidResource(dslType, resource, sourceLocation)) {
                    val rawResourceInfo = rawResourceFactory.rawResourceInfo(resource as FullResource, sourceLocation)

                    processor.invoke(rawResourceInfo, applyActionOverwrite)
                }
            }

            override fun receive(envYamlSource: EnvYamlSource, applyActionOverwrite: ApplyActionOverwrite) {
                // TODO #36 transfer yaml resources to processor.invoke ( yaml to RawResourceInfo(...) )
            }
        })

        methodCallContext.method.invoke(methodCallContext.typeInstance)
    }
}