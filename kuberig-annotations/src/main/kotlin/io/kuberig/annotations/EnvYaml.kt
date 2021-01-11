package io.kuberig.annotations

/**
 * Marker annotation for methods that add resources from a YAML-file.
 */
annotation class EnvYaml(
    /**
     * Name of the group these resources belongs to.
     */
    val group: String = "",
    /**
     * Default action to use to apply the resource(s).
     *
     * Can be overwritten on the individual resource emit calls made in this function.
     */
    val defaultAction: ApplyAction = ApplyAction.CREATE_OR_UPDATE
)
