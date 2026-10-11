package com.shopping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import shopping.config.ShoppingProperties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
public class InsecureRestClientConfig {

    @Bean
    RestClient.Builder insecureRestClientBuilder(
            ShoppingProperties properties
    ) throws Exception {
        TrustManager[] trustAllCertificates = {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(
                            X509Certificate[] chain,
                            String authType
                    ) {
                    }

                    @Override
                    public void checkServerTrusted(
                            X509Certificate[] chain,
                            String authType
                    ) {
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
                null,
                trustAllCertificates,
                new SecureRandom()
        );

        HttpClient httpClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(properties.shopApiTimeout())
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(
                properties.shopApiTimeout()
        );

        return RestClient.builder()
                .requestFactory(requestFactory);
    }
}