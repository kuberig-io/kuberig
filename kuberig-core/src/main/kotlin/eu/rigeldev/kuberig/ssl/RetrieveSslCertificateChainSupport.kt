package eu.rigeldev.kuberig.ssl

import org.apache.http.conn.ssl.TrustAllStrategy
import org.apache.http.ssl.SSLContexts
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSocket

class RetrieveSslCertificateChainSupport {

    fun retrieveSslCertificateChain(host: String, port: Int): SslCertificateChain {

        val chain = mutableListOf<X509Certificate>()

        val context = SSLContexts.custom()
            .loadTrustMaterial(null, TrustAllStrategy())
            .build()

        val socketFactory = context.socketFactory
        val socket = socketFactory.createSocket(host, port) as SSLSocket

        socket.addHandshakeCompletedListener { event ->
            event.peerCertificates.forEach {
                if (it is X509Certificate) {
                    chain.add(it)
                }
            }
        }

        socket.use {
            it.startHandshake()
        }

        return SslCertificateChain(chain)
    }

}

fun main() {
    val sslCertificateChain = RetrieveSslCertificateChainSupport().retrieveSslCertificateChain("www.rigel.dev", 443)

    println("sslCertificateChain.isRealChain() = ${sslCertificateChain.isRealChain()}")

    sslCertificateChain.subjectIssuers().forEach {
        println("${it.first} issued by ${it.second}")
    }
}