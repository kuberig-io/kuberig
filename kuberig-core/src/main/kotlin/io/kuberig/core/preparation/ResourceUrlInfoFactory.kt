package io.kuberig.core.preparation

import io.kuberig.core.deployment.ApiServerUrlSupport

class ResourceUrlInfoFactory(
    private val apiServerUrl: String,
    private val apiResourceInfoSource: APIResourceInfoSource
) {

    fun resourceUrl(apiVersion: String, kind: String, resourceName: String): ResourceUrlInfo {
        when (val lookup = this.apiResourceInfoSource.apiResource(apiVersion, kind)) {
            is KnownAPIResourceInfo -> {
                val apiVersionBaseUrl = ApiServerUrlSupport.apiVersionBaseUrl(apiServerUrl, apiVersion)
                val apiResource = lookup.apiResource

                check(!apiResource.namespaced) { "resourceUrl called for namespaced apiResource $apiResource" }

                val resourceTypeUrl = ApiServerUrlSupport.resourceTypeUrl(apiVersionBaseUrl, apiResource.name)
                val resourceUrl = ApiServerUrlSupport.resourceUrl(resourceTypeUrl, resourceName)

                return ResourceUrlInfo(apiResource, resourceTypeUrl, resourceUrl)
            }
            is UnknownAPIResourceInfo -> {
                throw IllegalStateException("Unknown $kind, unable to proceed.")
            }
        }
    }

    fun namespacedResourceUrl(
        apiVersion: String,
        kind: String,
        resourceName: String,
        namespace: String
    ): ResourceUrlInfo {
        when(val lookup = this.apiResourceInfoSource.apiResource(apiVersion, kind)) {
            is KnownAPIResourceInfo -> {
                val apiVersionBaseUrl = ApiServerUrlSupport.apiVersionBaseUrl(apiServerUrl, apiVersion)
                val apiResource = lookup.apiResource

                check(apiResource.namespaced) { "namespacedResourceUrl called for not namespaced apiResource $apiResource" }

                val resourceTypeUrl =
                    ApiServerUrlSupport.namespacedResourceTypeUrl(apiVersionBaseUrl, namespace, apiResource.name)
                val resourceUrl = ApiServerUrlSupport.resourceUrl(resourceTypeUrl, resourceName)

                return ResourceUrlInfo(apiResource, resourceTypeUrl, resourceUrl)
            }
            is UnknownAPIResourceInfo -> {
                throw IllegalStateException("Unknown $kind, unable to proceed.")
            }
        }
    }

}