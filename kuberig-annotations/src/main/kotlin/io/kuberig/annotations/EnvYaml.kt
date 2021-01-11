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
     * Action to use to apply the resource(s).
     */
    val action: ApplyAction = ApplyAction.CREATE_OR_UPDATE
)
