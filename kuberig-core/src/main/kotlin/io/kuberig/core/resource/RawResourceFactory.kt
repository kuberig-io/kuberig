package io.kuberig.core.resource

import org.json.JSONObject
import org.json.JSONTokener

class RawResourceFactory(private val defaultNamespace: String) {

    /**
     * Serialize the resource to JSON and extract some key information from it.
     */
    fun rawResourceInfo(resource: Any, sourceLocation: String): RawResourceInfo {
        val resourceJson = JSONObject(JSONTokener(ResourceSerializer.writeValueAsString(resource)))

        return rawResourceInfo(resourceJson, sourceLocation)
    }

    fun rawResourceInfo(resourceJson: JSONObject, sourceLocation: String): RawResourceInfo {
        val apiVersion = resourceJson.getString("apiVersion").toLowerCase()
        val kind = resourceJson.getString("kind")
        val metadataJson: JSONObject = resourceJson.getJSONObject("metadata")
        val resourceName = metadataJson.getString("name")

        val namespace = if (metadataJson.has("namespace")) {
            metadataJson.getString("namespace")
        } else {
            this.defaultNamespace
        }

        if (!metadataJson.has("namespace")) {
            metadataJson.put("namespace", namespace)
        }

        return RawResourceInfo(
            apiVersion,
            kind,
            resourceName,
            namespace,
            resourceJson,
            sourceLocation
        )
    }

}