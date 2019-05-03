package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod

data class ResourceGeneratorMethodResult(
    val method : ResourceGeneratorMethod,
    val resource : Any)