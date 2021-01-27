package io.kuberig.core.resource

import io.kuberig.core.preparation.ResourceUrlInfo
import org.json.JSONObject

data class RawJsonResourceInfo(
    val apiVersion: String,
    val kind: String,
    val resourceName: String,
    val namespaceValue: NamespaceValue,
    val json: JSONObject,
    val sourceLocation: String,
    val resourceUrlInfo: ResourceUrlInfo
) {
    constructor(source: RawJsonResourceInfo, newJson: JSONObject)
            : this(source.apiVersion, source.kind, source.resourceName, source.namespaceValue, newJson, source.sourceLocation, source.resourceUrlInfo)

    fun infoText(): String {
        return "$kind - $resourceName in $namespaceValue namespace"
    }

    fun fullInfoText(): String {
        return infoText() + " from resource generator method $sourceLocation "
    }
}

sealed class NamespaceValue
object Clustered: NamespaceValue()
data class Namespaced(val namespace: String): NamespaceValue()