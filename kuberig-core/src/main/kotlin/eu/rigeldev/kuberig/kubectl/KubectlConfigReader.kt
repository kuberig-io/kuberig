package eu.rigeldev.kuberig.kubectl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.jayway.jsonpath.JsonPath
import eu.rigeldev.kuberig.core.generation.yaml.ByteArrayDeserializer
import eu.rigeldev.kuberig.core.generation.yaml.ByteArraySerializer
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.security.KeyStore
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Pointer to the go types kubectl is using to read the kubectl config file:
 *
 * https://github.com/kubernetes/kubernetes/blob/ee4776d7ecb0e186c377e273d72f1ae957c44170/staging/src/k8s.io/client-go/tools/clientcmd/api/types.go#L31-L53
 *
 */
class KubectlConfigReader {

    fun readKubectlConfig(): ContextResult {

        val yamlFactory = YAMLFactory()
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)

        val objectMapper = ObjectMapper(yamlFactory)
        objectMapper.findAndRegisterModules()

        val byteArrayModule = SimpleModule()
        byteArrayModule.addSerializer(ByteArray::class.java, ByteArraySerializer())
        byteArrayModule.addDeserializer(ByteArray::class.java, ByteArrayDeserializer())

        objectMapper.registerModule(byteArrayModule)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

        val configFileLocation = System.getenv("KUBECONFIG") ?: "${System.getProperty("user.home")}/.kube/config"

        val configFile = File(configFileLocation)

        val tree = objectMapper.readTree(configFile)

        val currentContextName = tree.get("current-context").textValue()

        println("Using current-context: $currentContextName")

        val contextsNode = tree.get("contexts") as ArrayNode
        val clustersNode = tree.get("clusters") as ArrayNode
        val usersNode = tree.get("users") as ArrayNode

        val contextDetail = contextDetail(contextsNode, currentContextName)

        if (contextDetail != null) {
            var clusterDetail = clusterDetail(clustersNode, contextDetail.clusterName)

            if (clusterDetail != null) {
                println("Using server: ${clusterDetail.server}")
                println("Using namespace: ${contextDetail.namespace}")
                println("Using ca-certificate: \n ${clusterDetail.certificateAuthorityData}")


            } else {
                return ErrorContextResult("Could not find cluster details for cluster with name ${contextDetail.clusterName} referenced from current context $currentContextName")
            }

            var userDetail = userDetail(usersNode, contextDetail.userName)

            if (userDetail != null) {
                if (userDetail is ClientCertificateUserDetail) {

                    println("Using client-certificate: \n ${userDetail.clientCertificateData}")
                    println("Using client-key: \n ${userDetail.clientKeyData}")

                } else if (userDetail is AccessTokenUserDetail) {
                    println("Using access-token: \n ${userDetail.accessToken}")
                } else {
                    return ErrorContextResult("User detail type not supported")
                }
            } else {
                return ErrorContextResult("Could not find user details for user with name ${contextDetail.userName} referenced from current context $currentContextName")
            }

            val authDetails = this.buildAuthDetails(clusterDetail, userDetail)

            return if (authDetails == null) {
                ErrorContextResult("Failed to create authentication details")
            } else {
                OkContextResult(
                        clusterDetail,
                        userDetail,
                        authDetails,
                        contextDetail.namespace
                )
            }

        } else {
            return ErrorContextResult("Could not find context details for context with name $currentContextName")
        }
    }

    private fun buildAuthDetails(clusterDetail: ClusterDetail, userDetail: UserDetail): AuthDetails? {
        if (userDetail is ClientCertificateUserDetail) {
            Security.addProvider(BouncyCastleProvider())

            val pp = PEMParser(userDetail.clientKeyData.reader())
            val pemKeyPair = pp.readObject() as PEMKeyPair
            val kp = JcaPEMKeyConverter().getKeyPair(pemKeyPair)
            pp.close()

            val certificateFactory = CertificateFactory.getInstance("X.509")
            val cert = certificateFactory.generateCertificate(userDetail.clientCertificateData.byteInputStream()) as X509Certificate

            val caCert = certificateFactory.generateCertificate(clusterDetail.certificateAuthorityData.byteInputStream()) as X509Certificate

            val keyStore = KeyStore.getInstance("JKS")
            val keyStorePass = "changeit"
            keyStore.load(null)
            keyStore.setKeyEntry("user", kp.private, keyStorePass.toCharArray(), arrayOf(cert, caCert))

            return ClientCertAuthDetails(keyStore, keyStorePass)
        } else if (userDetail is AccessTokenUserDetail){
            return AccessTokenAuthDetail(userDetail.accessToken)
        } else {
            return null
        }
    }

    private fun contextDetail(contextsNode: ArrayNode, currentContextName: String): ContextDetail? {
        var contextDetail: ContextDetail? = null

        var currentIndex = 0
        while (contextDetail == null && currentIndex < contextsNode.size()) {
            val currentNode = contextsNode.get(currentIndex)

            if (currentNode.get("name").textValue() == currentContextName) {
                val contextNode = currentNode.get("context")

                val namespace = if (contextNode.has("namespace")) {
                    contextNode.get("namespace").textValue()
                } else {
                    "default"
                }

                contextDetail = ContextDetail(
                        contextNode.get("cluster").textValue(),
                        contextNode.get("user").textValue(),
                        namespace
                )
            }

            currentIndex++
        }
        return contextDetail
    }

    private fun clusterDetail(clustersNode: ArrayNode, clusterName: String): ClusterDetail? {
        var clusterDetail: ClusterDetail? = null

        var currentIndex = 0
        while (clusterDetail == null && currentIndex < clustersNode.size()) {
            val currentNode = clustersNode.get(currentIndex)

            if (currentNode.get("name").textValue() == clusterName) {
                val clusterNode = currentNode.get("cluster")

                val certificateAuthorityData = if (clusterNode.has("certificate-authority")) {
                    File(clusterNode.get("certificate-authority").textValue()).readText()
                } else {
                    String(Base64.getDecoder().decode(clusterNode.get("certificate-authority-data").textValue()))
                }

                clusterDetail = ClusterDetail(
                        certificateAuthorityData,
                        clusterNode.get("server").textValue()
                )
            }

            currentIndex++
        }

        return clusterDetail
    }

    private fun userDetail(usersNode: ArrayNode, userName: String): UserDetail? {
        var userDetail: UserDetail? = null

        var currentIndex = 0
        while (userDetail == null && currentIndex < usersNode.size()) {
            val currentNode = usersNode.get(currentIndex)

            if (currentNode.get("name").textValue() == userName) {
                val userNode = currentNode.get("user") as ObjectNode

                userDetail = toUserDetail(userNode)
            }

            currentIndex++
        }

        return userDetail
    }

    private fun toUserDetail(userNode: ObjectNode): UserDetail? {
        if (userNode.has("exec")) {
            val execNode = userNode.get("exec")

            val command = execNode.get("command").textValue()
            val argsNode = execNode.get("args") as ArrayNode

            var fullCommand = command

            var currentIndex = 0
            while (currentIndex < argsNode.size()) {
                val currentNode = argsNode.get(currentIndex)

                fullCommand = "$fullCommand ${currentNode.textValue()}"

                currentIndex++
            }

            // todo deal with 'env' node
            val execCredentialsObject = JSONObject(fullCommand.runCommand())

            val statusObject = execCredentialsObject.getJSONObject("status")

            if (statusObject.has("clientCertificateData") && statusObject.has("clientKeyData")) {
                return ClientCertificateUserDetail(
                    statusObject.getString("clientCertificateData"),
                    statusObject.getString("clientKeyData")
                )
            } else if (statusObject.has("token")) {
                return AccessTokenUserDetail(statusObject.getString("token"))
            } else {
                println("Unsupported exec credentials json encountered")
                return null
            }
        }
        else if (userNode.has("client-certificate") && userNode.has("client-key")) {
            return ClientCertificateUserDetail(
                    File(userNode.get("client-certificate").textValue()).readText(),
                    File(userNode.get("client-key").textValue()).readText()
            )
        }
        else if (userNode.has("auth-provider")) {
            val authProviderNode = userNode.get("auth-provider")

            val configNode = authProviderNode.get("config")

            val validAccessTokenAvailable = if (configNode.has("expiry") && configNode.has("access-token")) {
                val expiry = Instant.parse(configNode.get("expiry").textValue())
                val now = Instant.now()

                now.isBefore(expiry)
            } else {
                false
            }

            val accessToken = if (validAccessTokenAvailable) {
                configNode.get("access-token").textValue()
            } else {
                val cmdArgs = configNode.get("cmd-args").textValue()
                val cmdPath = configNode.get("cmd-path").textValue()
                val tokenKey = configNode.get("token-key").textValue()

                val fullCommand = "$cmdPath $cmdArgs"

                val output = fullCommand.runCommand()

                val tokenJsonPath = tokenKey.replace("{", "$").replace("}", "")
                JsonPath.parse(output).read(tokenJsonPath)
            }

            return AccessTokenUserDetail(accessToken)
        }
        else {
            println("User configuration is not supported yet \n $userNode")
            return null
        }
    }

}

data class ContextDetail(val clusterName: String, val userName: String, val namespace: String = "")
data class ClusterDetail(val certificateAuthorityData: String, val server: String)
sealed class UserDetail
data class ClientCertificateUserDetail(val clientCertificateData: String, val clientKeyData: String) : UserDetail()
data class AccessTokenUserDetail(val accessToken: String) : UserDetail()

sealed class AuthDetails
data class ClientCertAuthDetails(val keyStore: KeyStore, val keyStorePass: String) : AuthDetails()
data class AccessTokenAuthDetail(val accessToken: String) : AuthDetails()
object NoAuthDetails : AuthDetails()

sealed class ContextResult

data class OkContextResult(
        val clusterDetail: ClusterDetail,
        val userDetail: UserDetail,
        val authDetails: AuthDetails,
        val namespace: String
) : ContextResult()

class ErrorContextResult(val error: String) : ContextResult()

fun String.runCommand(workingDir: File = File("."),
                      timeoutAmount: Long = 60,
                      timeoutUnit: TimeUnit = TimeUnit.SECONDS): String? {
    return try {
        ProcessBuilder(*this.split("\\s".toRegex()).toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)

                .start().apply {
                    waitFor(timeoutAmount, timeoutUnit)
                }.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}