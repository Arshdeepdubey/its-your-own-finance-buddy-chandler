package com.finance.service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.finance.dto.Secretdto;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest;
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
        return cache.get(secretName, name -> executeAwsCall(() -> {
            GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(name).build();
            return client.getSecretValue(request).secretString();
        }, "read secret '" + name + "'"));
    }

    public List<Secretdto> getSecrets() {
        return executeAwsCall(() -> client.listSecrets(ListSecretsRequest.builder().build()).secretList().stream()
            .map(secret -> new Secretdto(secret.name(), getSecret(secret.name())))
            .collect(Collectors.toList()), "list secrets");
    }

    public void createSecret (Secretdto dto){
        executeAwsCall(() -> {
            CreateSecretRequest request = CreateSecretRequest.builder().name(dto.getName()).secretString(dto.getValue()).build();
            client.createSecret(request);
            cache.put(dto.getName(), dto.getValue());
            return null;
        }, "create secret '" + dto.getName() + "'");
    }

    public void updateSecret(Secretdto dto) {
        executeAwsCall(() -> {
            PutSecretValueRequest request = PutSecretValueRequest.builder().secretId(dto.getName()).secretString(dto.getValue()).build();
            client.putSecretValue(request);
            cache.put(dto.getName(), dto.getValue());
            return null;
        }, "update secret '" + dto.getName() + "'");
    }

    public void deleteSecret(String secretName) {
        executeAwsCall(() -> {
            DeleteSecretRequest request = DeleteSecretRequest.builder().secretId(secretName).forceDeleteWithoutRecovery(true).build();
            client.deleteSecret(request);
            cache.invalidate(secretName);
            return null;
        }, "delete secret '" + secretName + "'");
    }

    private <T> T executeAwsCall(java.util.function.Supplier<T> action, String operationDescription) {
        try {
            return action.get();
        } catch (SdkClientException ex) {
            throw new IllegalStateException(
                "AWS credentials are missing or invalid. Set AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY (and AWS_SESSION_TOKEN if needed) or use IAM role-based credentials before calling Secrets Manager. Failed to "
                    + operationDescription + ".",
                ex);
        }
    }
}
