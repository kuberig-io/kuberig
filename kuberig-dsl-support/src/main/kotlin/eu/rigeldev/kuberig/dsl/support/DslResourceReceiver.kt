package eu.rigeldev.kuberig.dsl.support

import eu.rigeldev.kuberig.dsl.DslType

interface DslResourceReceiver {

    fun getName() : String

    fun <T> receive(dslType: DslType<T>)

}