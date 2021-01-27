package io.kuberig.core.preparation

class DelegatingAPIResourceInfoSource(private val delegates: List<APIResourceInfoSource>) : APIResourceInfoSource {

    override fun apiResource(apiVersion: String, kind: String): APIResourceInfo {
        var apiResource: APIResourceInfo = UnknownAPIResourceInfo

        val delegatesIterator = delegates.iterator()

        while(apiResource == UnknownAPIResourceInfo && delegatesIterator.hasNext()) {
            apiResource = delegatesIterator.next().apiResource(apiVersion, kind)
        }

        return apiResource
    }
}