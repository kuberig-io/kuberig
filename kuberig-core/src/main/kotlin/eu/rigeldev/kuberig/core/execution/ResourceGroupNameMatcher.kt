package eu.rigeldev.kuberig.core.execution

interface ResourceGroupNameMatcher {

    fun matchResourceGroupName(definedName: String?): Boolean

}