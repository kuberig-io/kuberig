package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.core.annotations.EnvResource
import eu.rigeldev.kuberig.core.annotations.EnvResources
import java.lang.reflect.Method

class GroupResourceGenerationFilter(private val groupNameMatcher: ResourceGroupNameMatcher) : ResourceGeneratorFilter {

    override fun shouldGenerate(method: Method): Boolean {
        val envResourceAnnotation = method.getDeclaredAnnotation(EnvResource::class.java)
        val envResourcesAnnotation = method.getDeclaredAnnotation(EnvResources::class.java)

        val definedResourceGroupName : String? = if (envResourceAnnotation != null) {
            if (envResourceAnnotation.group == "") {
                null
            } else {
                envResourceAnnotation.group
            }
        } else {
            if (envResourcesAnnotation.group == "") {
                null
            } else {
                envResourcesAnnotation.group
            }
        }

        return groupNameMatcher.matchResourceGroupName(definedResourceGroupName)
    }
}