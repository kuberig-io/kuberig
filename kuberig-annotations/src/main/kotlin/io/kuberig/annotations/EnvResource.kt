package io.kuberig.annotations

/**
 * Marker annotation for methods that generate a single environment resource.
 * The generated resource is published as return value from the method.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnvResource(
    /**
     * Name of the group this resource belongs to.
     */
    val group: String = "",
    /**
     * Action to use to apply the resource.
     */
    val action: ApplyAction = ApplyAction.CREATE_OR_UPDATE
)