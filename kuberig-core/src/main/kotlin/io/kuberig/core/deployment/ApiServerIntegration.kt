package io.kuberig.core.deployment

import io.kuberig.cluster.client.ClusterClientBuilder
import io.kuberig.config.KubeRigFlags
import io.kuberig.core.preparation.APIResource
import io.kuberig.core.preparation.APIResourceList
import io.kuberig.core.preparation.ResourceUrlInfo
import io.kuberig.core.resource.RawJsonResourceInfo
import io.kuberig.kubectl.AuthDetails
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import kong.unirest.UnirestInstance
import org.json.JSONObject

class ApiServerIntegration(
    certificateAuthorityData: String?,
    authDetails: AuthDetails,
    flags: KubeRigFlags
) {
    private val unirestInstance: UnirestInstance

    init {
        unirestInstance = Unirest.primaryInstance()

        ClusterClientBuilder(flags, unirestInstance)
            .initializeClient(certificateAuthorityData, authDetails)
    }

    fun getResource(resourceUrlInfo: ResourceUrlInfo): GetResourceResult {
        val getRequest = unirestInstance.get(resourceUrlInfo.resourceUrl)
        val getResult = getRequest.asJson()

        return if (getResult.status == 404) {
            UnknownGetResourceResult(resourceUrlInfo)
        } else {
            val currentJson = getResult.body.`object`
            val resourceVersion = currentJson.getJSONObject("metadata").getString("resourceVersion")
            ExistsGetResourceResult(resourceUrlInfo, currentJson, resourceVersion)
        }
    }

    fun postResource(rawJsonResourceInfo: RawJsonResourceInfo, resourceUrlInfo: ResourceUrlInfo): PostResourceResult {
        val postResponse = unirestInstance.post(resourceUrlInfo.resourceTypeUrl)
            .header("Content-Type", "application/json")
            .body(rawJsonResourceInfo.json)
            .asString()

        return if (postResponse.status == 201) {
            SuccessPostResourceResult(resourceUrlInfo, rawJsonResourceInfo)
        } else {
            FailedPostResourceResult(resourceUrlInfo, rawJsonResourceInfo, postResponse)
        }
    }

    fun serverSideApplyResourcePatch(
        rawJsonResourceInfo: RawJsonResourceInfo,
        resourceUrlInfo: ResourceUrlInfo,
        force: Boolean
    ): ServerSideApplyPatchResourceResult {
        val patchResponse = unirestInstance.patch(resourceUrlInfo.resourceUrl)
            .header("Content-Type", "application/apply-patch+yaml")
            .accept("application/json")
            .queryString("fieldManager", "kuberig")
            .queryString("force", force)
            .body(rawJsonResourceInfo.json)
            .asString()

        return if (patchResponse.status == 200 || patchResponse.status == 201) {
            SuccessServerSideApplyPatchResourceResult(resourceUrlInfo, rawJsonResourceInfo)
        } else {
            FailedServerSideApplyPatchResourceResult(resourceUrlInfo, rawJsonResourceInfo, patchResponse)
        }
    }

    fun putResource(updateJsonResourceInfo: RawJsonResourceInfo, resourceUrlInfo: ResourceUrlInfo): PutResourceResult {
        val putResponse = unirestInstance.put(resourceUrlInfo.resourceUrl)
            .header("Content-Type", "application/json")
            .body(updateJsonResourceInfo.json)
            .asString()

        return when (putResponse.status) {
            422 -> {
                ConflictPutResourceResult(resourceUrlInfo, updateJsonResourceInfo, putResponse)
            }
            200 -> {
                SuccessPutResourceResult(resourceUrlInfo, updateJsonResourceInfo)
            }
            else -> {
                FailedPutResourceResult(resourceUrlInfo, updateJsonResourceInfo, putResponse)
            }
        }
    }

    fun deleteResource(resourceUrlInfo: ResourceUrlInfo): DeleteResourceResult {
        val deleteResponse = unirestInstance.delete(resourceUrlInfo.resourceUrl)
            .header("Content-Type", "application/json")
            .asString()

        return if (deleteResponse.status == 200) {
            SuccessDeleteResourceResult(resourceUrlInfo)
        } else {
            FailedDeleteResourceResult(resourceUrlInfo, deleteResponse)
        }
    }

    fun retrieveApiResources(apiVersionBaseUrl: String): List<APIResource> {
        return unirestInstance.get(apiVersionBaseUrl)
            .asObject(APIResourceList::class.java)
            .body
            .resources
    }

    fun shutDown() {
        this.unirestInstance.shutDown()
    }

}

abstract class ResourceResult(val urlInfo: ResourceUrlInfo) {
    abstract fun logInfo(applyStrategy: ApplyStrategy<Any>)

    protected fun showResponseDetails(response: HttpResponse<String>) {
        println(response.status)
        println(response.statusText)

        if (response.headers["Content-Type"][0] == "application/json") {
            val json = JSONObject(response.body)
            println(json.getString("message"))
        } else {
            println(response.body)
        }
    }
}

sealed class GetResourceResult(urlInfo: ResourceUrlInfo) : ResourceResult(urlInfo)
class ExistsGetResourceResult(urlInfo: ResourceUrlInfo, val json: JSONObject, val resourceVersion: String) :
    GetResourceResult(urlInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("${urlInfo.resourceUrl} exists")
    }
}

class UnknownGetResourceResult(urlInfo: ResourceUrlInfo) : GetResourceResult(urlInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("${urlInfo.resourceUrl} does not exist")
    }
}

sealed class PostResourceResult(urlInfo: ResourceUrlInfo, val rawJsonResourceInfo: RawJsonResourceInfo) :
    ResourceResult(urlInfo)

class FailedPostResourceResult(
    urlInfo: ResourceUrlInfo,
    rawJsonResourceInfo: RawJsonResourceInfo,
    val response: HttpResponse<String>
) : PostResourceResult(urlInfo, rawJsonResourceInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        super.showResponseDetails(this.response)
        println("Failed to post(${applyStrategy.name}) ${rawJsonResourceInfo.fullInfoText()}")
    }
}

class SuccessPostResourceResult(urlInfo: ResourceUrlInfo, rawJsonResourceInfo: RawJsonResourceInfo) :
    PostResourceResult(urlInfo, rawJsonResourceInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("post(${applyStrategy.name}) ${rawJsonResourceInfo.infoText()} - success")
    }
}

sealed class ServerSideApplyPatchResourceResult(
    urlInfo: ResourceUrlInfo,
    val rawJsonResourceInfo: RawJsonResourceInfo
) : ResourceResult(urlInfo)

class FailedServerSideApplyPatchResourceResult(
    urlInfo: ResourceUrlInfo,
    rawJsonResourceInfo: RawJsonResourceInfo,
    val response: HttpResponse<String>
) : ServerSideApplyPatchResourceResult(urlInfo, rawJsonResourceInfo) {

    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        super.showResponseDetails(this.response)
        println("Failed to patch(${applyStrategy.name}) ${rawJsonResourceInfo.fullInfoText()}")
    }
}

class SuccessServerSideApplyPatchResourceResult(
    urlInfo: ResourceUrlInfo,
    rawJsonResourceInfo: RawJsonResourceInfo
) : ServerSideApplyPatchResourceResult(urlInfo, rawJsonResourceInfo) {

    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("patch(${applyStrategy.name}) ${rawJsonResourceInfo.infoText()} - success")
    }
}

sealed class PutResourceResult(
    urlInfo: ResourceUrlInfo,
    val updateJsonResourceInfo: RawJsonResourceInfo
) : ResourceResult(urlInfo)

class ConflictPutResourceResult(
    urlInfo: ResourceUrlInfo,
    updateJsonResourceInfo: RawJsonResourceInfo,
    val response: HttpResponse<String>
) : PutResourceResult(urlInfo, updateJsonResourceInfo) {

    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        super.showResponseDetails(this.response)
        println("Failed(conflict) to put(${applyStrategy.name}) ${updateJsonResourceInfo.fullInfoText()}")
    }
}

class FailedPutResourceResult(
    urlInfo: ResourceUrlInfo,
    updateJsonResourceInfo: RawJsonResourceInfo,
    val response: HttpResponse<String>
) : PutResourceResult(urlInfo, updateJsonResourceInfo) {

    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        super.showResponseDetails(this.response)
        println("Failed to put(${applyStrategy.name}) ${updateJsonResourceInfo.fullInfoText()}")
    }
}

class SuccessPutResourceResult(
    urlInfo: ResourceUrlInfo,
    updateJsonResourceInfo: RawJsonResourceInfo
) : PutResourceResult(urlInfo, updateJsonResourceInfo) {

    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("put(${applyStrategy.name}) ${updateJsonResourceInfo.infoText()} - success")
    }
}

sealed class DeleteResourceResult(urlInfo: ResourceUrlInfo) : ResourceResult(urlInfo)
class FailedDeleteResourceResult(urlInfo: ResourceUrlInfo, val response: HttpResponse<String>) :
    DeleteResourceResult(urlInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        super.showResponseDetails(this.response)
        println("Failed to delete(${applyStrategy.name}) ${urlInfo.resourceUrl}")
    }
}

class SuccessDeleteResourceResult(urlInfo: ResourceUrlInfo) : DeleteResourceResult(urlInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("delete(${applyStrategy.name}) ${urlInfo.resourceUrl} - success")
    }
}