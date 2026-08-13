package com.finance.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
public class AwsConfig {
    
    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.secretsmanager.endpoint}")
    private String endpointOverride;

    @Bean
    public SecretsManagerClient secretManagerClient() {
        var builder = SecretsManagerClient.builder().region(Region.of(region));

        //enables local mocking if endpoint is enabled
        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }
}
