package com.finance.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.finance.dto.Secretdto;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;

@Service
public class SecretService {
    
    private final SecretsManagerClient client;
    private final Cache<String, String> cache;

    public SecretService(SecretsManagerClient client){
        this.client=client;

        // TTL cache to reduce AWS API billing & latency
        this.cache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100).build();
    }

    public String getSecret(String secretName) {
        return cache.get(secretName, name -> {
            GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(name).build();
            return client.getSecretValue(request).secretString();
        });
    }

    public void createSecret (Secretdto dto){
        CreateSecretRequest request = CreateSecretRequest.builder().name(dto.getName()).secretString(dto.getValue()).build();
        client.createSecret(request);
        cache.put(dto.getName(), dto.getValue());
    }

    public void updateSecret(Secretdto dto) {
        PutSecretValueRequest request = PutSecretValueRequest.builder().secretId(dto.getName()).secretString(dto.getValue()).build();
        client.putSecretValue(request);
        cache.put(dto.getName(), dto.getValue());
    }

    public void deleteSecret(String secretName) {
        DeleteSecretRequest request = DeleteSecretRequest.builder().secretId(secretName).forceDeleteWithoutRecovery(true).build();
        client.deleteSecret(request);
        cache.invalidate(secretName);
    }
}
