package com.ks.tocho5.config;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class R2Config {

    private static final Logger log = LoggerFactory.getLogger(R2Config.class);

    @Value("${r2.endpoint:}")
    private String endpoint;

    @Value("${r2.accessKeyId:}")
    private String accessKeyId;

    @Value("${r2.secretAccessKey:}")
    private String secretAccessKey;

    @Bean
    public S3Client r2S3Client() {
        String ak = accessKeyId;
        String sk = secretAccessKey;

        if (ak == null || ak.isBlank() || sk == null || sk.isBlank()) {
            log.warn("R2 credenciales vacías: S3Client se crea con valores dummy. " +
                     "Las subidas a R2 van a fallar hasta que definas R2_ACCESS_KEY_ID y R2_SECRET_ACCESS_KEY.");
            // Dummy values suficientes para que el bean construya; cualquier op falla con 401/403 en runtime.
            ak = "missing-access-key-id";
            sk = "missing-secret-access-key";
        }

        String url = (endpoint == null || endpoint.isBlank())
                ? "https://r2.not-configured.local"
                : endpoint;

        return S3Client.builder()
                .endpointOverride(URI.create(url))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(ak, sk)
                        )
                )
                .region(Region.US_EAST_1) // R2 ignora región pero hay que poner algo
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true) // IMPORTANTE para R2
                                .build()
                )
                .build();
    }
}
