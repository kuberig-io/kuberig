package io.kuberig.core.model

/**
 * Flattened version of a type with annotated methods for one specific method.
 */
data class GeneratorMethodAndType(
    val typeName: String,
    val methodType: GeneratorMethodType,
    val methodName: String
) {
    fun fullMethod(): String {
        return "$typeName.$methodName"
    }
}
