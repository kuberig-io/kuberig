package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.core.annotations.EnvResource
import java.lang.reflect.Method

class GroupResourceGenerationFilter(val groupNameMatcher: ResourceGroupNameMatcher) : ResourceGeneratorFilter {

    override fun shouldGenerate(method: Method): Boolean {
        val envResourceAnnotation = method.getDeclaredAnnotation(EnvResource::class.java)
        val definedResourceGroupName : String? = if (envResourceAnnotation?.group == "") {
            null
        } else {
            envResourceAnnotation?.group
        }

        return groupNameMatcher.matchResourceGroupName(definedResourceGroupName)
    }
}