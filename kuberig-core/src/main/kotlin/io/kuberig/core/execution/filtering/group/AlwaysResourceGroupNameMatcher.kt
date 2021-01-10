package io.kuberig.core.execution.filtering.group

class AlwaysResourceGroupNameMatcher : ResourceGroupNameMatcher {
    override fun matchResourceGroupName(definedName: String?): Boolean {
        return true
    }
}