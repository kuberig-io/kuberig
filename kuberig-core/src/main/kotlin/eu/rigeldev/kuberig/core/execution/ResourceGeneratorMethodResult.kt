package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.core.detection.ResourceGeneratorMethod

sealed class ResourceGeneratorMethodResult(val method : ResourceGeneratorMethod)

class SuccessResult(
    method: ResourceGeneratorMethod,
    val resource : Any) : ResourceGeneratorMethodResult(method)

class SkippedResult(method: ResourceGeneratorMethod) : ResourceGeneratorMethodResult(method)

class ErrorResult(method: ResourceGeneratorMethod,
                  val rootCause : Throwable) : ResourceGeneratorMethodResult(method) {

    fun errorMessage(): String {

        return if (rootCause.message != null) {
            rootCause.message
        } else {
            rootCause.cause?.message
        } ?: ""

    }

}