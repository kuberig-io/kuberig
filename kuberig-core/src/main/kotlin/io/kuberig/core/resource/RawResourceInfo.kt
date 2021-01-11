package io.kuberig.core.resource

import org.json.JSONObject

data class RawResourceInfo(
    val apiVersion: String,
    val kind: String,
    val resourceName: String,
    val namespace: String,
    val json: JSONObject,
    val sourceLocation: String
) {
    constructor(source: RawResourceInfo, newJson: JSONObject)
            : this(source.apiVersion, source.kind, source.resourceName, source.namespace, newJson, source.sourceLocation)

    fun infoText(): String {
        return "$kind - $resourceName in $namespace namespace"
    }

    fun fullInfoText(): String {
        return infoText() + " from resource generator method $sourceLocation "
    }
}