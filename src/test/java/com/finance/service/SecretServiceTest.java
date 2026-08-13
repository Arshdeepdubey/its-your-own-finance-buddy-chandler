package com.finance.service;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.finance.dto.Secretdto;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueResponse;

class SecretServiceTest {

    @Test
    void shouldReturnSecretValueFromAwsWhenCacheMisses() {
        SecretService service = new SecretService(fakeSecretsManagerClient());

        String value = service.getSecret("db-password");

        assertEquals("secret-value", value);
    }

    @Test
    void shouldCreateSecretAndCacheIt() {
        SecretService service = new SecretService(fakeSecretsManagerClient());
        Secretdto dto = new Secretdto("api-key", "abc");

        service.createSecret(dto);

        assertEquals("abc", service.getSecret("api-key"));
    }

    @Test
    void shouldThrowHelpfulExceptionWhenAwsCredentialsAreMissing() {
        SecretService service = new SecretService(fakeMissingCredentialsClient());

        IllegalStateException exception = assertThrows(IllegalStateException.class, service::getSecrets);

        assertTrue(exception.getMessage().contains("AWS credentials"));
    }

    private static SecretsManagerClient fakeMissingCredentialsClient() {
        return (SecretsManagerClient) Proxy.newProxyInstance(
            SecretsManagerClient.class.getClassLoader(),
            new Class<?>[] { SecretsManagerClient.class },
            (proxy, method, args) -> {
                throw software.amazon.awssdk.core.exception.SdkClientException.builder()
                    .message("Unable to load credentials from any of the providers in the chain")
                    .build();
            }
        );
    }

    private static SecretsManagerClient fakeSecretsManagerClient() {
        Map<String, String> store = new HashMap<>();
        store.put("db-password", "secret-value");

        return (SecretsManagerClient) Proxy.newProxyInstance(
            SecretsManagerClient.class.getClassLoader(),
            new Class<?>[] { SecretsManagerClient.class },
            (proxy, method, args) -> switch (method.getName()) {
                case "getSecretValue" -> {
                    GetSecretValueRequest request = (GetSecretValueRequest) args[0];
                    yield GetSecretValueResponse.builder().secretString(store.getOrDefault(request.secretId(), "")).build();
                }
                case "createSecret" -> {
                    CreateSecretRequest request = (CreateSecretRequest) args[0];
                    store.put(request.name(), request.secretString());
                    yield CreateSecretResponse.builder().name(request.name()).arn("arn:aws:secretsmanager:us-east-1:000000000000:secret:" + request.name()).build();
                }
                case "putSecretValue" -> {
                    PutSecretValueRequest request = (PutSecretValueRequest) args[0];
                    store.put(request.secretId(), request.secretString());
                    yield PutSecretValueResponse.builder().arn("arn:aws:secretsmanager:us-east-1:000000000000:secret:" + request.secretId()).build();
                }
                case "deleteSecret" -> {
                    DeleteSecretRequest request = (DeleteSecretRequest) args[0];
                    store.remove(request.secretId());
                    yield DeleteSecretResponse.builder().arn("arn:aws:secretsmanager:us-east-1:000000000000:secret:" + request.secretId()).build();
                }
                case "close" -> null;
                case "serviceName" -> "secretsmanager";
                case "toString" -> "FakeSecretsManagerClient";
                default -> null;
            }
        );
    }
}
