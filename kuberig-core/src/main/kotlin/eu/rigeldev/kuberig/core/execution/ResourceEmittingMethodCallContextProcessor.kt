package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.dsl.KubernetesResourceDslType
import eu.rigeldev.kuberig.dsl.model.BasicResource
import eu.rigeldev.kuberig.dsl.model.FullResource
import eu.rigeldev.kuberig.dsl.support.DslResourceEmitter
import eu.rigeldev.kuberig.dsl.support.DslResourceReceiver

class ResourceEmittingMethodCallContextProcessor : MethodCallContextProcessor {

    override fun process(methodCallContext: MethodCallContext, resources: MutableList<FullResource>) {

        DslResourceEmitter.registerReceiver(object : DslResourceReceiver {
            override fun getName(): String {
                return "default-receiver"
            }

            override fun <T: BasicResource> receive(dslType: KubernetesResourceDslType<T>) {
                val resource = dslType.toValue()

                MethodCallContextProcessorResourceFilter.filteringAdd(
                    dslType,
                    resource,
                    resources,
                    {
                        val e = IllegalStateException("")

                        var userCallLocation : String? = null
                        val stackIterator = e.stackTrace.iterator()

                        while (userCallLocation == null && stackIterator.hasNext()) {
                            val stackTraceElement = stackIterator.next()

                            if (stackTraceElement.className == methodCallContext.type.name) {
                                userCallLocation = stackTraceElement.toString()
                            }
                        }

                        userCallLocation
                    }
                )
            }
        })

        methodCallContext.method.invoke(methodCallContext.typeInstance)
    }
}