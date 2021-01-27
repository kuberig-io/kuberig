package io.kuberig.core.preparation

import io.kuberig.dsl.support.TargetNamespace
import org.json.JSONObject

data class InitialResourceInfo(
    val apiVersion: String,
    val kind: String,
    val resourceName: String,
    val sourceLocation: String,
    val targetNamespace: TargetNamespace,
    val initialJson: JSONObject
)