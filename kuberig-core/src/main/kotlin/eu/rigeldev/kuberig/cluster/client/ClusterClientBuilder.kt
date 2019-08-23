package eu.rigeldev.kuberig.cluster.client

import com.fasterxml.jackson.databind.ObjectMapper
import eu.rigeldev.kuberig.config.KubeRigFlags
import eu.rigeldev.kuberig.kubectl.AccessTokenAuthDetail
import eu.rigeldev.kuberig.kubectl.AuthDetails
import eu.rigeldev.kuberig.kubectl.ClientCertAuthDetails
import eu.rigeldev.kuberig.kubectl.NoAuthDetails
import kong.unirest.UnirestInstance
import kong.unirest.apache.ApacheClient
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustAllStrategy
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.BasicHttpClientConnectionManager
import org.apache.http.ssl.SSLContexts
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext

class ClusterClientBuilder(private val flags: KubeRigFlags,
                           private val objectMapper: ObjectMapper,
                           private val unirestInstance: UnirestInstance) {

    fun initializeClient(certificateAuthorityData: String?, authDetails: AuthDetails) {

        unirestInstance.config().objectMapper = object : kong.unirest.ObjectMapper {
            override fun writeValue(value: Any?): String {
                return objectMapper.writeValueAsString(value)
            }

            override fun <T : Any?> readValue(value: String?, valueType: Class<T>?): T {
                return objectMapper.readValue(value, valueType)
            }
        }

        var keyStore: KeyStore? = null
        var keyStorePass: String? = null

        when (authDetails) {
            is ClientCertAuthDetails -> {
                keyStore = authDetails.keyStore
                keyStorePass = authDetails.keyStorePass
            }
            is AccessTokenAuthDetail -> {
                unirestInstance.config().clearDefaultHeaders()
                unirestInstance.config().setDefaultHeader("Authorization", "Bearer ${authDetails.accessToken}")
            }
            is NoAuthDetails -> {
                println("Connecting without authentication")
            }
            else -> println("Auth details not supported")
        }

        val sslContextBuilder = SSLContexts.custom()

        if (keyStore != null && keyStorePass != null) {
            sslContextBuilder.loadKeyMaterial(keyStore, keyStorePass.toCharArray())
        }

        val sslcontext : SSLContext = if (certificateAuthorityData == null) {
            when {
                flags.trustAllSSL -> sslContextBuilder
                    .loadTrustMaterial(null, TrustAllStrategy())
                    .build()
                flags.trustSelfSignedSSL -> sslContextBuilder
                    .loadTrustMaterial(null, TrustSelfSignedStrategy())
                    .build()
                else -> SSLContext.getDefault()
            }
        } else {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val caCert = certificateFactory.generateCertificate(certificateAuthorityData.byteInputStream()) as X509Certificate

            val trustStore = KeyStore.getInstance("JKS")
            trustStore.load(null)
            trustStore.setCertificateEntry("cluster-ca-cert", caCert)

            sslContextBuilder
                .loadTrustMaterial(trustStore, null)
                .build()
        }

        val sslsf = SSLConnectionSocketFactory(sslcontext)

        val registry = RegistryBuilder.create<ConnectionSocketFactory>()
            .register("https", sslsf)
            .register("http", PlainConnectionSocketFactory())
            .build()
        val ccm = BasicHttpClientConnectionManager(registry)

        val clientBuilder = HttpClientBuilder.create()

        clientBuilder.setSSLSocketFactory(sslsf)
        clientBuilder.setConnectionManager(ccm)

        val httpClient = clientBuilder.build()
        unirestInstance.config().httpClient(ApacheClient.builder(httpClient))
    }

}