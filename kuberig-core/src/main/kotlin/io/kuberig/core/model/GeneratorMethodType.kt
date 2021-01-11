package io.kuberig.core.model

/**
 * Specifies how the detected resource method should be executed.
 */
enum class GeneratorMethodType {
    /**
     * @EnvResource annotated methods are returning a single resource.
     */
    RESOURCE_RETURNING,

    /**
     * @EnvResources annotated methods emit can emit multiple resources.
     */
    RESOURCE_EMITTING,

    /**
     * @EnvYaml annotated methods read YAML file and emit multiple raw resources.
     */
    RAW_YAML
}
