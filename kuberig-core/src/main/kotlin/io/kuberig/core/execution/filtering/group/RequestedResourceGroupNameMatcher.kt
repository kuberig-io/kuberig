package io.kuberig.core.execution.filtering.group

class RequestedResourceGroupNameMatcher(private val requestedGroupName: String) : ResourceGroupNameMatcher {
    override fun matchResourceGroupName(definedName: String?): Boolean {
        return if (definedName != null) {
            requestedGroupName.toLowerCase() == definedName.toLowerCase()
        } else {
            false
        }
    }
}