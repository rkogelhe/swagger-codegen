package com.wordnik.swagger.codegen.util

import com.wordnik.swagger.model._

import java.net._
import java.io.InputStream
import javax.net.ssl._
import java.security.cert.X509Certificate
import java.security.SecureRandom

import scala.io.Source

trait RemoteUrl {
  var previousSSLSocketFactory: SSLSocketFactory = null
  var previousHostnameVerifier: HostnameVerifier = null
  
	def urlToString(url: String, authorization: Option [AuthorizationValue], checkServerCert: Boolean = true): String = {
		var is: InputStream = null
		try{
		  if (!checkServerCert) {
		    installLenientTrustManager
		  }
			val conn: URLConnection = authorization match {
				case Some(auth: ApiKeyValue) => {
					if(auth.passAs == "header") {
						val connection = new URL(url).openConnection()
						connection.setRequestProperty(auth.keyName, auth.value)
						connection
					}
					else if(auth.passAs == "query") {
						new URL(url + "?%s=%s".format(URLEncoder.encode(auth.keyName), URLEncoder.encode(auth.value))).openConnection()
					}
					else {
						new URL(url).openConnection()
					}
				}
				case None => new URL(url).openConnection()
			}
			is = conn.getInputStream()
			Source.fromInputStream(is).mkString
		}
		finally {
		  if (!checkServerCert) {
		    revertTrustManager
		  }
			if(is != null) is.close()
		}
	}
	
	def installLenientTrustManager = {
    previousSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory()
	  previousHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
	  
	  val trustAllCerts = new X509TrustManager {
      def getAcceptedIssuers: Array[X509Certificate] = {
        return Array.empty;
      }
    
      def checkClientTrusted(certs: Array[X509Certificate], authType: String) = {
      }
    
      def checkServerTrusted(certs: Array[X509Certificate], authType: String) = {
      }
    }

	  val sc = SSLContext.getInstance("SSL")
	  sc.init(null, Array(trustAllCerts), new SecureRandom())

	  HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
	  
	  val allHostsValid = new HostnameVerifier {
	    def verify(hostname: String, session: SSLSession): Boolean = {
        true
      }
	  }
	  
	  HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
	}
	
	def revertTrustManager = {
    HttpsURLConnection.setDefaultSSLSocketFactory(previousSSLSocketFactory)
    HttpsURLConnection.setDefaultHostnameVerifier(previousHostnameVerifier)
	}
}