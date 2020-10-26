package io.kuberig.core.execution

import io.kuberig.core.detection.ResourceGeneratorMethod

sealed class ResourceGeneratorMethodResult(val method : ResourceGeneratorMethod)

class SuccessResult(
        method: ResourceGeneratorMethod,
        val resources : List<Any>,
        val tick: Int) : ResourceGeneratorMethodResult(method)

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