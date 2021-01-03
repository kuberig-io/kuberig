package io.kuberig.core.execution

import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.FullResource
import org.slf4j.LoggerFactory

object ResourceValidator {
    private val logger = LoggerFactory.getLogger(ResourceValidator::class.java)

    fun isValidResource(
        dslType: KubernetesResourceDslType<FullResource>,
        userCallLocationProvider: () -> String?
    ): Boolean {
        val resourcePackageName = dslType::class.java.packageName

        return if (resourcePackageName.startsWith("kinds.")) {
            true
        } else {
            val userCallLocation: String? = userCallLocationProvider()

            logger.error(dslType::class.java.name + " is not within the kinds.* package, skipping. [${userCallLocation}]")
            false
        }
    }

}