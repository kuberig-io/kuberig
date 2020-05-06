package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.dsl.KubernetesResourceDslType
import eu.rigeldev.kuberig.dsl.model.BasicResource
import eu.rigeldev.kuberig.dsl.model.FullResource
import org.slf4j.LoggerFactory

object MethodCallContextProcessorResourceFilter {
    private val logger = LoggerFactory.getLogger(MethodCallContextProcessorResourceFilter::class.java)

    fun <T: BasicResource> filteringAdd(dslType: KubernetesResourceDslType<T>, resource: T, resources: MutableList<FullResource>, userCallLocationProvider: () -> String?) {
        val resourcePackageName = dslType::class.java.packageName

        val userCallLocation : String? = userCallLocationProvider()

        if (resourcePackageName.startsWith("kinds.")) {
            if (resource is FullResource ) {
                resources.add(resource)
            } else {
                logger.error(resource::class.java.name + " is not a eu.rigeldev.kuberig.dsl.model.FullResource, skipping. [${userCallLocation}]")
            }
        } else {
            logger.error(resource::class.java.name + " is not within the kinds.* package, skipping. [${userCallLocation}]")
        }
    }

}