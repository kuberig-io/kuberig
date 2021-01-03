package io.kuberig.core.model

sealed class GeneratorMethodResult(val method: GeneratorMethodAndType) {
    fun fullMethodName(): String {
        return this.method.fullMethod()
    }
}

class SuccessResult(
    method: GeneratorMethodAndType,
    val resourceApplyRequests: List<ResourceApplyRequest>
) : GeneratorMethodResult(method)

class SkippedResult(method: GeneratorMethodAndType) : GeneratorMethodResult(method)
class ErrorResult(
    method: GeneratorMethodAndType,
    val rootCause: Throwable
) : GeneratorMethodResult(method)