package io.kuberig.core.model

/**
 * Result of a method annotated with @EnvResource of @EnvResources.
 */
sealed class GeneratorMethodResult(val method: GeneratorMethodAndType) {
    fun fullMethodName(): String {
        return this.method.fullMethod()
    }
}

/**
 * Success result. The method was executed successfully.
 *
 * We do not require there to be resources. A warning is logged by the executor but we can't assume this is an error.
 */
class SuccessResult(
    method: GeneratorMethodAndType,
    val resourceApplyRequests: List<ResourceApplyRequest>
) : GeneratorMethodResult(method)

/**
 * Skipped result, the method execution was not needed.
 *
 * Reasons for this can be:
 * - The environment that is being generated for is not mentioned in the @EnvFilter annotation on the method.
 * - The group specified on the @EnvResource or @EnvResources annotation is not specified on the deploy or generateYaml task command line.
 */
class SkippedResult(method: GeneratorMethodAndType) : GeneratorMethodResult(method)

/**
 * Error result.
 *
 * Reasons for this can be:
 * - coding/runtime/configuration/error
 * - in case of @EnvResource annotated method, the return type may not be correct. It needs to be a type in the kinds.* package.
 */
class ErrorResult(
    method: GeneratorMethodAndType,
    val rootCause: Throwable
) : GeneratorMethodResult(method)