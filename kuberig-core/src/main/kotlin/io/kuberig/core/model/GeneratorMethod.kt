package io.kuberig.core.model

/**
 * Single annotated method with it's type.
 */
data class GeneratorMethod(val generatorMethodType: GeneratorMethodType, val methodName: String)