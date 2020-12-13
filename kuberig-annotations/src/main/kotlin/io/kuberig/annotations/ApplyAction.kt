package io.kuberig.annotations

/**
 * What actions should be taken to apply a resource.
 */
enum class ApplyAction {
    /**
     * Only create the resource if it does not exist. Useful for volumes.
     */
    CREATE_ONLY,

    /**
     * Creates the resource if it does not exist. Otherwise updates it.
     *
     * This is the default apply action.
     */
    CREATE_OR_UPDATE,

    /**
     * Will always create the resource. If the resource already exists it will be deleted first.
     *
     * Use this for resources were you are changing immutable attributes like the host or ssl certificates on
     * OpenShift routes.
     */
    RECREATE
}