package io.kuberig.core.preparation

interface APIResourceInfoSource {

    fun apiResource(apiVersion: String, kind: String): APIResourceInfo
}

sealed class APIResourceInfo
object UnknownAPIResourceInfo: APIResourceInfo()
data class KnownAPIResourceInfo(val apiResource: APIResource): APIResourceInfo()