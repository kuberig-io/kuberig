package io.kuberig.core.model

data class GeneratorMethodAndType(
    val typeName: String,
    val methodType: GeneratorMethodType,
    val methodName: String
) {
    fun fullMethod(): String {
        return "$typeName.$methodName"
    }
}
