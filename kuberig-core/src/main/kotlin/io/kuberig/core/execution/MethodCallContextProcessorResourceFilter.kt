package io.kuberig.core.execution

import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource
import io.kuberig.dsl.model.FullResource
import org.slf4j.LoggerFactory

object MethodCallContextProcessorResourceFilter {
    private val logger = LoggerFactory.getLogger(MethodCallContextProcessorResourceFilter::class.java)

    fun <T: BasicResource> filteringAdd(dslType: KubernetesResourceDslType<T>, resource: T, resources: MutableList<FullResource>, userCallLocationProvider: () -> String?) {
        val resourcePackageName = dslType::class.java.packageName

        if (resourcePackageName.startsWith("kinds.") && resource is FullResource ) {
            resources.add(resource)
        } else {
            val userCallLocation : String? = userCallLocationProvider()

            logger.error(resource::class.java.name + " is not within the kinds.* package, skipping. [${userCallLocation}]")
        }
    }

}