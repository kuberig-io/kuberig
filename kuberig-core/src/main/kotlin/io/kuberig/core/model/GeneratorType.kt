package io.kuberig.core.model

/**
 * While detection is ongoing, classes are processed and GeneratorType instances are emitted.
 */
class GeneratorType(
    /**
     * The absolute type name.
     */
    val typeName: String,
    /**
     * All the methods annotated.
     * These can be from anywhere in the hierarchy.
     */
    val generatorMethods: List<GeneratorMethod>)