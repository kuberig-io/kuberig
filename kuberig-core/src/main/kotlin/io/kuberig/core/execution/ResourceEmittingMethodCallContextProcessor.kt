package io.kuberig.core.execution

import io.kuberig.core.preparation.InitialResourceInfo
import io.kuberig.core.preparation.InitialResourceInfoFactory
import io.kuberig.core.resource.EnvYamlSourceService
import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource
import io.kuberig.dsl.model.FullResource
import io.kuberig.dsl.support.ApplyActionOverwrite
import io.kuberig.dsl.support.DslResourceEmitter
import io.kuberig.dsl.support.DslResourceReceiver
import io.kuberig.dsl.support.TargetNamespace
import io.kuberig.dsl.support.yaml.EnvYamlSource

class ResourceEmittingMethodCallContextProcessor(
    private val initialResourceInfoFactory: InitialResourceInfoFactory,
    private val envYamlSourceService: EnvYamlSourceService
) : MethodCallContextProcessor {

    override fun process(
        methodCallContext: MethodCallContext,
        processor: (initialResourceInfo: InitialResourceInfo, applyActionOverwrite: ApplyActionOverwrite) -> Unit
    ) {

        DslResourceEmitter.registerReceiver(object : DslResourceReceiver {
            override fun getName(): String {
                return "default-receiver"
            }

            override fun <T : BasicResource> receive(
                dslType: KubernetesResourceDslType<T>,
                applyActionOverwrite: ApplyActionOverwrite,
                targetNamespace: TargetNamespace
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
                    val initialResourceInfo = initialResourceInfoFactory.create(
                        resource as FullResource,
                        sourceLocation,
                        targetNamespace
                    )

                    processor.invoke(initialResourceInfo, applyActionOverwrite)
                }
            }

            override fun receive(
                envYamlSource: EnvYamlSource,
                applyActionOverwrite: ApplyActionOverwrite
            ) {
                envYamlSourceService.importEnvYamlSource(envYamlSource).forEach {
                    processor.invoke(it, applyActionOverwrite)
                }
            }
        })

        methodCallContext.method.invoke(methodCallContext.typeInstance)
    }
}