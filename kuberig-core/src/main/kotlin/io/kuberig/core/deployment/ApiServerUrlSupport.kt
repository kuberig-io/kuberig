package io.kuberig.core.deployment

/**
 * Knows the structure of the API server URLs.
 */
object ApiServerUrlSupport {

    private fun apiOrApis(apiVersion: String): String {
        return if (apiVersion == "v1") {
            "api"
        } else {
            "apis"
        }
    }

    fun apiVersionBaseUrl(apiServerUrl: String, apiVersion: String): String {
        val apiOrApisPart = apiOrApis(apiVersion)

        return "$apiServerUrl/$apiOrApisPart/$apiVersion"
    }

    fun resourceTypeUrl(apiVersionBaseUrl: String, resourceTypeName: String): String {
        return "$apiVersionBaseUrl/$resourceTypeName"
    }

    fun namespacedResourceTypeUrl(apiVersionBaseUrl: String, namespace: String, resourceTypeName: String): String {
        return "$apiVersionBaseUrl/namespaces/$namespace/$resourceTypeName"
    }

    fun resourceUrl(resourceTypeUrl: String, resourceName: String): String {
        return "$resourceTypeUrl/$resourceName"
    }

}