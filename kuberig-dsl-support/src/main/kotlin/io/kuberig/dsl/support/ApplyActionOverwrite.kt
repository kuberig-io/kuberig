package io.kuberig.dsl.support

import io.kuberig.annotations.ApplyAction

sealed class ApplyActionOverwrite

/**
 * Use the action specified in the method annotation.
 */
object UseDefault : ApplyActionOverwrite()

/**
 * Use the action specified in this overwrite.
 */
data class UseOverwrite(val action: ApplyAction) : ApplyActionOverwrite()
