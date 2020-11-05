package io.kuberig.core.execution

import java.lang.reflect.Method

/**
 * ResourceGeneratorFilter makes it possible to skip the execution of a resource generation method.
 */
interface ResourceGeneratorFilter {

    /**
     * Return true if the execution of the method is needed.
     *
     * @param method The method that needs to be evaluated.
     */
    fun shouldGenerate(method: Method): Boolean

}