package eu.rigeldev.kuberig.core.deploy

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.request.HttpRequest
import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.deploy.control.DeployControl
import eu.rigeldev.kuberig.core.deploy.control.TickGateKeeper
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorMethodResult
import eu.rigeldev.kuberig.core.execution.SuccessResult
import eu.rigeldev.kuberig.encryption.EncryptionSupportFactory
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import java.io.File

class ResourceDeployer(private val projectDirectory: File,
                       private val environment: KubeRigEnvironment,
                       private val deployControl: DeployControl,
                       private val resourceGenerationRuntimeClasspathClassLoader: ClassLoader,
                       private val encryptionSupportFactory: EncryptionSupportFactory) {

    val objectMapper = ObjectMapper()

    init {
        objectMapper.findAndRegisterModules()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
    }

    fun deploy(methodResults : List<ResourceGeneratorMethodResult>) {

        Unirest.setObjectMapper(object: com.mashape.unirest.http.ObjectMapper {
            override fun writeValue(value: Any?): String {
                return objectMapper.writeValueAsString(value)
            }

            override fun <T : Any?> readValue(value: String?, valueType: Class<T>?): T {
                return objectMapper.readValue(value, valueType)
            }
        })

        val sslcontext = SSLContexts.custom()
            .loadTrustMaterial(null, TrustSelfSignedStrategy())
            .build()

        val sslsf = SSLConnectionSocketFactory(sslcontext)
        val httpclient = HttpClients.custom()
            .setSSLSocketFactory(sslsf)
            .build()
        Unirest.setHttpClient(httpclient)

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
            val json = objectMapper.valueToTree<JsonNode>(methodResult.resource)

            val apiVersion = json.get("apiVersion").textValue().toLowerCase()
            val kind = json.get("kind").textValue()
            val resourceName = json.get("metadata").get("name").textValue()

            val getResourceList = Unirest.get("${environment.apiServer}/api/$apiVersion")
            this.addAuthentication(getResourceList)
            val apiResourceList = getResourceList
                .asObject(APIResourceList::class.java)

            val apiResource = apiResourceList.body.resources.first{ it.kind == kind }

            val namespace = if (json.get("metadata").hasNonNull("namespace")) {
                json.get("metadata").get("namespace").textValue()
            } else {
                "default"
            }

            val targetUrl = "${environment.apiServer}/api/$apiVersion/namespaces/$namespace/${apiResource.name}"

            val get = Unirest.get("$targetUrl/$resourceName")
            this.addAuthentication(get)
            val getResult = get
                .asJson()

            if (getResult.status == 404) {
                val post = Unirest.post(targetUrl)
                this.addAuthentication(post)
                val postResponse = post
                        .header("Content-Type", "application/json")
                        .body(json)
                        .asString()

                if (postResponse.status != 201) {
                    println(postResponse.status)
                    println(postResponse.statusText)
                    println(postResponse.body)
                    System.out.println("Failed to create $kind from resource generator method ${methodResult.method.generatorType} - ${methodResult.method.methodName}")
                } else {
                    println("created $kind - $resourceName in $namespace namespace")
                }
            } else {
                val put = Unirest.put("$targetUrl/$resourceName")
                this.addAuthentication(put)
                val putResponse = put
                        .header("Content-Type", "application/json")
                        .body(json)
                        .asString()

                if (putResponse.status != 200) {
                    println(putResponse.status)
                    println(putResponse.statusText)
                    println(putResponse.body)
                    System.out.println("Failed to update $kind from resource generator method ${methodResult.method.generatorType} - ${methodResult.method.methodName}")
                } else {
                    println("updated $kind - $resourceName in $namespace namespace")
                }
            }
        }

        return methodResult
    }

    private fun addAuthentication(request: HttpRequest) {
        val environmentDirectory = File(projectDirectory, "environments/${this.environment.name}")
        val encryptedAccessTokenFile = File(environmentDirectory, ".encrypted.${this.environment.name}.access-token")

        if (encryptedAccessTokenFile.exists()) {
            val environmentEncryptionSupport = this.encryptionSupportFactory.forEnvironment(
                this.projectDirectory,
                this.environment)

            val decryptedAccessTokenFile = environmentEncryptionSupport.decryptFile(encryptedAccessTokenFile)

            request.header("Authorization", "Bearer ${decryptedAccessTokenFile.readText()}")

            decryptedAccessTokenFile.delete()
        } else {
            println(encryptedAccessTokenFile.absolutePath + " not available")
        }
    }
}