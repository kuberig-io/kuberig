package eu.rigeldev.kuberig.core.execution

import java.lang.reflect.Method

/**
 * ResourceGeneratorFilter makes it possible to skip the execution of a resource generation method.
 */
interface ResourceGeneratorFilter {

    /**
     * Return true if the execution of the method is needed.
     *
     * @param method The method that needs to be evaluated.
     * @param runDetail The details of the resource generation that is current being executed.
     */
    fun shouldGenerate(method: Method): Boolean

}