package com.example.workshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Configuration for RestTemplate with optional SSL verification bypass.
 * WARNING: SSL bypass should only be used in development environments!
 */
@Configuration
public class RestTemplateConfig {

    @Value("${provider.ssl-verification-enabled:true}")
    private boolean sslVerificationEnabled;

    @Bean
    public RestTemplate restTemplate() {
        if (!sslVerificationEnabled) {
            return createInsecureRestTemplate();
        }
        return new RestTemplate();
    }

    /**
     * Creates a RestTemplate that bypasses SSL certificate verification.
     * DO NOT USE IN PRODUCTION!
     */
    private RestTemplate createInsecureRestTemplate() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // Create an ssl socket factory with our all-trusting manager
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Set the default SSL socket factory and hostname verifier
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(5000);

            return new RestTemplate(factory);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create insecure RestTemplate", e);
        }
    }
}
