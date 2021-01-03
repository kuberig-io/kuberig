package io.kuberig.core.execution.filtering.group

class NoResourceGroupNameMatcher : ResourceGroupNameMatcher {
    override fun matchResourceGroupName(definedName: String?): Boolean {
        return definedName == null
    }
}