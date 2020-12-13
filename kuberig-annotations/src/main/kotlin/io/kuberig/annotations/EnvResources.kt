package io.kuberig.annotations

/**
 * Marker annotation for methods that generate multiple resources.
 * The generated resources are published by calling one or multiple emit functions on io.kuberig.dsl.support.DslResourceEmitter.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnvResources(
    /**
     * Name of the group this resource belongs to.
     */
    val group: String = "",
    /**
     * Default action to use to apply the resource(s).
     *
     * Can be overwritten on the individual resource emit calls made in this function.
     */
    val defaultAction: ApplyAction = ApplyAction.CREATE_OR_UPDATE
)