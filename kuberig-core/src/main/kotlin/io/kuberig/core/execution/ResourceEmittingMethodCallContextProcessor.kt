package io.kuberig.core.execution

import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource
import io.kuberig.dsl.model.FullResource
import io.kuberig.dsl.support.ApplyActionOverwrite
import io.kuberig.dsl.support.DslResourceEmitter
import io.kuberig.dsl.support.DslResourceReceiver

class ResourceEmittingMethodCallContextProcessor : MethodCallContextProcessor {

    override fun process(
        methodCallContext: MethodCallContext,
        processor: (resource: FullResource, applyActionOverwrite: ApplyActionOverwrite) -> Unit) {

        DslResourceEmitter.registerReceiver(object : DslResourceReceiver {
            override fun getName(): String {
                return "default-receiver"
            }

            override fun <T : BasicResource> receive(dslType: KubernetesResourceDslType<T>, applyActionOverwrite: ApplyActionOverwrite) {
                val userCallLocationProvider = {
                    val e = IllegalStateException("")

                    var userCallLocation: String? = null
                    val stackIterator = e.stackTrace.iterator()

                    while (userCallLocation == null && stackIterator.hasNext()) {
                        val stackTraceElement = stackIterator.next()

                        if (stackTraceElement.className == methodCallContext.type.name) {
                            userCallLocation = stackTraceElement.toString()
                        }
                    }

                    userCallLocation
                }

                val resource = dslType.toValue()

                if (ResourceValidator.isValidResource(dslType, resource, userCallLocationProvider)) {
                    processor.invoke(resource as FullResource, applyActionOverwrite)
                }
            }
        })

        methodCallContext.method.invoke(methodCallContext.typeInstance)
    }
}