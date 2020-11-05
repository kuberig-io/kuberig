package io.kuberig.kubectl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.jayway.jsonpath.JsonPath
import io.kuberig.core.generation.yaml.ByteArrayDeserializer
import io.kuberig.core.generation.yaml.ByteArraySerializer
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.ssl.SSLContexts
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.StringWriter
import java.net.URL
import java.security.KeyStore
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSocket


/**
 * Pointer to the go types kubectl is using to read the kubectl config file:
 *
 * https://github.com/kubernetes/kubernetes/blob/ee4776d7ecb0e186c377e273d72f1ae957c44170/staging/src/k8s.io/client-go/tools/clientcmd/api/types.go#L31-L53
 *
 */
class KubectlConfigReader {

    private val logger = LoggerFactory.getLogger(KubectlConfigReader::class.java)

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

        val configFileLocation = System.getenv("KUBECONFIG") ?: "${System.getenv("HOME")}/.kube/config"

        val configFile = File(configFileLocation)

        val tree = objectMapper.readTree(configFile)

        val currentContextName = tree.get("current-context").textValue()

        logger.info("Using current-context: $currentContextName")

        val contextsNode = tree.get("contexts") as ArrayNode
        val clustersNode = tree.get("clusters") as ArrayNode
        val usersNode = tree.get("users") as ArrayNode

        val contextDetail = contextDetail(contextsNode, currentContextName)

        if (contextDetail != null) {
            val clusterDetail = clusterDetail(clustersNode, contextDetail.clusterName)

            if (clusterDetail != null) {
                logger.info("Using server: ${clusterDetail.server}")
                logger.info("Using namespace: ${contextDetail.namespace}")
                logger.info("Using ca-certificate: \n ${clusterDetail.certificateAuthorityData}")


            } else {
                return ErrorContextResult("Could not find cluster details for cluster with name ${contextDetail.clusterName} referenced from current context $currentContextName")
            }

            val userDetail = userDetail(usersNode, contextDetail.userName)

            if (userDetail != null) {
                when (userDetail) {
                    is ClientCertificateUserDetail -> {

                        logger.debug("Using client-certificate: \n ${userDetail.clientCertificateData}")
                        logger.debug("Using client-key: \n ${userDetail.clientKeyData}")

                    }
                    is AccessTokenUserDetail -> {
                        logger.debug("Using access-token: \n ${userDetail.accessToken}")
                    }
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
                    val namespaceValue = contextNode.get("namespace").textValue()
                    if (namespaceValue == "") {
                        "default"
                    } else {
                        namespaceValue
                    }
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

                val server = clusterNode.get("server").textValue()

                val certificateAuthorityData = if (clusterNode.has("certificate-authority")) {
                    File(clusterNode.get("certificate-authority").textValue()).readText()
                } else if (clusterNode.has("certificate-authority-data")) {
                    String(Base64.getDecoder().decode(clusterNode.get("certificate-authority-data").textValue()))
                } else {
                    retrieveCertificateAuthorityDataFromServer(server)
                }

                clusterDetail = ClusterDetail(certificateAuthorityData, server)
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
                logger.error("Unsupported exec credentials json encountered")
                return null
            }
        }
        else if (userNode.has("client-certificate") && userNode.has("client-key")) {
            return ClientCertificateUserDetail(
                    File(userNode.get("client-certificate").textValue()).readText(),
                    File(userNode.get("client-key").textValue()).readText()
            )
        }
        else if (userNode.has("client-certificate-data") && userNode.has("client-key-data")) {
            return ClientCertificateUserDetail(
                String(Base64.getDecoder().decode(userNode.get("client-certificate-data").textValue())),
                String(Base64.getDecoder().decode(userNode.get("client-key-data").textValue()))
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
        else if (userNode.has("token")) {
            return AccessTokenUserDetail(userNode.get("token").textValue())
        }
        else {
            logger.error("User configuration is not supported yet \n $userNode")
            return null
        }
    }

    /**
     * Not completely happy with this part as it does not yet go up the certificate chain.
     */
    private fun retrieveCertificateAuthorityDataFromServer(url: String): String {
        println("Failing back to retrieve certificate from $url, we may not get the CA but only the server certificate in this case.")

        var result : String? = null

        val tm = SavingTrustManager()

        val context = SSLContexts.custom()
                .loadTrustMaterial(null, tm)
                .build()

        val urlObject = URL(url)

        val factory = context.socketFactory
        val socket = factory.createSocket(urlObject.host, urlObject.port) as SSLSocket
        socket.soTimeout = 10000
        try {
            socket.use {
                it.startHandshake()
            }
        }
        catch (e: SSLException) {
            // ignore
        }

        if (tm.chain == null) {
            println("Could not obtain server certificate chain")
        } else {
            val certList = tm.chain!!.asList()
            if (certList.isNotEmpty()) {
                // We should improve here and actually detect what the most top level certificate is.
                val certificate = certList[0]
                println("Using ${certificate.subjectDN}")
                println("Issued by ${certificate.issuerDN}")
                val stringWriter = StringWriter()
                JcaPEMWriter(stringWriter).use { pemWriter ->
                    pemWriter.writeObject(certificate)
                }
                result = stringWriter.toString()
            }
        }

        check(result != null) { "Failed to retrieve Certificate from $url" }

        return result
    }

}

class SavingTrustManager : TrustStrategy {

    var chain: Array<out X509Certificate>? = null

    override fun isTrusted(chain: Array<out X509Certificate>?, authType: String?): Boolean {
        this.chain = chain
        return false
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