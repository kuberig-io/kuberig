package eu.rigeldev.kuberig.core.deploy

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.deploy.control.DeployControl
import eu.rigeldev.kuberig.core.deploy.control.TickGateKeeper
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorMethodResult
import eu.rigeldev.kuberig.core.execution.SuccessResult
import eu.rigeldev.kuberig.encryption.EncryptionSupport
import eu.rigeldev.kuberig.encryption.EncryptionSupportFactory
import eu.rigeldev.kuberig.support.PropertiesLoaderSupport
import kong.unirest.HttpRequest
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import kong.unirest.apache.ApacheClient
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File

class ResourceDeployer(private val projectDirectory: File,
                       private val environment: KubeRigEnvironment,
                       private val deployControl: DeployControl,
                       private val resourceGenerationRuntimeClasspathClassLoader: ClassLoader,
                       private val encryptionSupportFactory: EncryptionSupportFactory) {

    val objectMapper = ObjectMapper()
    val environmentEncryptionSupport: EncryptionSupport
    val apiServerUrl : String

    init {
        objectMapper.findAndRegisterModules()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

        this.environmentEncryptionSupport = this.encryptionSupportFactory.forEnvironment(
            this.projectDirectory,
            this.environment)

        val environmentsDirectory = File(this.projectDirectory, "environments")
        val environmentDirectory = File(environmentsDirectory, environment.name)
        val environmentConfigsFile = File(environmentDirectory, "${environment.name}-configs.properties")

        val environmentConfigs = PropertiesLoaderSupport.loadProperties(environmentConfigsFile)

        val possiblyEncryptedApiServerUrl = environmentConfigs.getProperty("api.server.url")

        this.apiServerUrl = this.environmentEncryptionSupport.decryptValue(possiblyEncryptedApiServerUrl)
    }

    fun deploy(methodResults : List<ResourceGeneratorMethodResult>) {

        Unirest.config().objectMapper = object: kong.unirest.ObjectMapper {
            override fun writeValue(value: Any?): String {
                return objectMapper.writeValueAsString(value)
            }

            override fun <T : Any?> readValue(value: String?, valueType: Class<T>?): T {
                return objectMapper.readValue(value, valueType)
            }
        }

        val sslcontext = SSLContexts.custom()
            .loadTrustMaterial(null, TrustSelfSignedStrategy())
            .build()

        val sslsf = SSLConnectionSocketFactory(sslcontext)
        val httpclient = HttpClients.custom()
            .setSSLSocketFactory(sslsf)
            .build()
        Unirest.config().httpClient(ApacheClient.builder(httpclient))

        if (this.deployControl.tickRange.isEmpty()) {
            methodResults.forEach { this.deploy(it) }
        } else {
            val successResults = methodResults
                .filter { it.javaClass == SuccessResult::class.java }
                .map {it as SuccessResult}

            val tickIterator = this.deployControl.tickRange.iterator()
            var currentTick = 0

            @Suppress("UNCHECKED_CAST") val tickGateKeeperType : Class<out TickGateKeeper> = resourceGenerationRuntimeClasspathClassLoader.loadClass(this.deployControl.tickGateKeeper) as Class<out TickGateKeeper>
            val tickGateKeeper = tickGateKeeperType.getConstructor().newInstance()

            var gateOpen = true
            println("[TICK-SYSTEM] starting...")

            while (gateOpen && tickIterator.hasNext()) {
                val nextTick = tickIterator.nextInt()

                gateOpen = tickGateKeeper.isGateOpen(currentTick, nextTick)

                if (gateOpen) {

                    val tickResources = successResults
                        .filter { it.tick == nextTick }

                    println("[TICK-SYSTEM][TICK#$nextTick] deploying ${tickResources.size} resource(s).")

                    tickResources.forEach { this.deploy(it) }

                }

                if (gateOpen && tickIterator.hasNext()) {
                    println("[TICK-SYSTEM] next tick in ${this.deployControl.tickDuration}.")

                    Thread.sleep(this.deployControl.tickDuration.toMillis())

                    currentTick = nextTick
                }

            }

            if (gateOpen) {
                println("[TICK-SYSTEM] success.")
            } else {
                println("[TICK-SYSTEM] error - gate keeper closed gate at tick $currentTick!")
                throw IllegalStateException("Tick gate keeper closed gate at tick $currentTick!")
            }
        }
    }

    private fun deploy(methodResult: ResourceGeneratorMethodResult): ResourceGeneratorMethodResult {


        if (methodResult is SuccessResult) {
            val newJson = JSONObject(JSONTokener(objectMapper.writeValueAsString(methodResult.resource)))

            val apiVersion = newJson.getString("apiVersion").toLowerCase()
            val kind = newJson.getString("kind")
            val resourceName = newJson.getJSONObject("metadata").getString("name")

            val namespace = if (newJson.getJSONObject("metadata").has("namespace")) {
                newJson.getJSONObject("metadata").getString("namespace")
            } else {
                "default"
            }

            println("------")
            println("deploying $kind - $resourceName in $namespace namespace...")

            val apiOrApisPart = if (apiVersion == "v1") {
                "api"
            } else {
                "apis"
            }

            val getResourceList = Unirest.get("$apiServerUrl/$apiOrApisPart/$apiVersion")
            this.addAuthentication(getResourceList)
            val apiResourceList = getResourceList
                .asObject(APIResourceList::class.java)

            val apiResource = apiResourceList.body.resources.first{ it.kind == kind }



            val targetUrl = "$apiServerUrl/$apiOrApisPart/$apiVersion/namespaces/$namespace/${apiResource.name}"

            val get = Unirest.get("$targetUrl/$resourceName")
            this.addAuthentication(get)
            val getResult = get
                .asJson()

            if (getResult.status == 404) {
                val post = Unirest.post(targetUrl)
                this.addAuthentication(post)
                val postResponse = post
                        .header("Content-Type", "application/json")
                        .body(newJson)
                        .asString()

                if (postResponse.status != 201) {
                    println(postResponse.status)
                    println(postResponse.statusText)
                    println(postResponse.body)
                    println("Failed to create $kind from resource generator method ${methodResult.method.generatorType} - ${methodResult.method.methodName}")
                } else {
                    println("created $kind - $resourceName in $namespace namespace")
                }
            } else {
                val put = Unirest.put("$targetUrl/$resourceName")
                this.addAuthentication(put)

                val currentObject = getResult.body.`object`

                val updateJson = JSONObject(newJson.toString())

                updateJson.getJSONObject("metadata").put("resourceVersion", currentObject.getJSONObject("metadata").getString("resourceVersion"))

                val putResponse = put
                        .header("Content-Type", "application/json")
                        .body(updateJson)
                        .asString()

                if (putResponse.status == 422) {
                    val currentJsonPathContext = JsonPath.parse(currentObject.toString())
                    val newJsonPathContext = JsonPath.parse(updateJson.toString())

                    val statusObject = JSONObject(putResponse.body)
                    val statusReason = statusObject.getString("reason")

                    var newJsonUpdated = false
                    var recreateNeeded = false

                    if (statusReason.toLowerCase() == "invalid") {

                        val detailsObject = statusObject.getJSONObject("details")
                        val causesArray = detailsObject.getJSONArray("causes")

                        var causesArrayIndex = 0
                        while (!recreateNeeded && causesArrayIndex < causesArray.length()) {
                            val causeObject = causesArray.getJSONObject(causesArrayIndex)

                            val causeReason = causeObject.getString("reason")
                            val causeMessage = causeObject.getString("message")
                            val causeField = causeObject.getString("field")

                            if (causeReason == "FieldValueInvalid" && causeMessage.endsWith("field is immutable")) {
                                // copy from original if the new object does not specify it
                                val fieldJsonPath = "$.$causeField"
                                val currentFieldValue = currentJsonPathContext.read<Any>(fieldJsonPath)

                                val newFieldValue = try {
                                    newJsonPathContext.read<Any>(fieldJsonPath)
                                } catch (e: PathNotFoundException) {
                                    null
                                }

                                if (newFieldValue == null) {
                                    val lastDotIndex = fieldJsonPath.lastIndexOf('.')
                                    val pathPart = fieldJsonPath.substring(0, lastDotIndex)
                                    val keyPart = fieldJsonPath.substring(lastDotIndex + 1)

                                    newJsonPathContext.put(pathPart, keyPart, currentFieldValue)

                                    newJsonUpdated = true
                                } else {
                                    // new resource definition - requires update of immutable field
                                    recreateNeeded = true
                                }
                            }

                            causesArrayIndex++
                        }

                    }

                    if (newJsonUpdated){

                        val retryPut = Unirest.put("$targetUrl/$resourceName")
                        this.addAuthentication(retryPut)

                        val retryJson = newJsonPathContext.jsonString()

                        val putRetryResponse = retryPut
                                .header("Content-Type", "application/json")
                                .body(retryJson)
                                .asString()

                        if (putRetryResponse.status != 200) {
                            this.putFailure(putRetryResponse, kind, methodResult)
                        } else {
                            println("updated $kind - $resourceName in $namespace namespace")
                        }

                    } else if (recreateNeeded) {

                        val delete = Unirest.delete("$targetUrl/$resourceName")
                        this.addAuthentication(delete)

                        val deleteResponse = delete.header("Content-Type", "application/json")
                                .asString()

                        if (deleteResponse.status == 200) {
                            val recreatePost = Unirest.post(targetUrl)
                            this.addAuthentication(recreatePost)
                            val recreatePostResponse = recreatePost
                                    .header("Content-Type", "application/json")
                                    .body(newJson)
                                    .asString()

                            if (recreatePostResponse.status != 201) {
                                println(recreatePostResponse.status)
                                println(recreatePostResponse.statusText)
                                println(recreatePostResponse.body)
                                println("Failed to create $kind from resource generator method ${methodResult.method.generatorType} - ${methodResult.method.methodName}")
                            } else {
                                println("re-created $kind - $resourceName in $namespace namespace")
                            }
                        }

                    } else {
                        this.putFailure(putResponse, kind, methodResult)
                    }

                } else if (putResponse.status != 200) {
                    this.putFailure(putResponse, kind, methodResult)
                }  else {
                    println("updated $kind - $resourceName in $namespace namespace")
                }
            }
        }

        return methodResult
    }

    private fun putFailure(putResponse: HttpResponse<String>, kind: String?, methodResult: ResourceGeneratorMethodResult) {
        println(putResponse.status)
        println(putResponse.statusText)
        println(putResponse.body)
        println("Failed to update $kind from resource generator method ${methodResult.method.generatorType} - ${methodResult.method.methodName}")
    }


    private fun <R:HttpRequest<R>> addAuthentication(request: R) {
        val environmentDirectory = File(projectDirectory, "environments/${this.environment.name}")
        val encryptedAccessTokenFile = File(environmentDirectory, ".encrypted.${this.environment.name}.access-token")

        if (encryptedAccessTokenFile.exists()) {
            val environmentEncryptionSupport = this.encryptionSupportFactory.forEnvironment(
                this.projectDirectory,
                this.environment)

            val decryptedAccessTokenFile = environmentEncryptionSupport.decryptFile(encryptedAccessTokenFile)
            val token = decryptedAccessTokenFile.readText()

            request.header("Authorization", "Bearer $token")

            decryptedAccessTokenFile.delete()
        }
    }
}