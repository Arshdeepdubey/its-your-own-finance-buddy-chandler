package com.finance.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.finance.dto.Secretdto;
import com.finance.service.SecretService;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueResponse;

class SecretControllerTest {

    @Test
    void shouldReturnSecretWhenFetched() {
        SecretController controller = new SecretController(new SecretService(fakeSecretsManagerClient()));

        ResponseEntity<Secretdto> response = controller.getSecret("db-password");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("db-password", response.getBody().getName());
        assertEquals("super-secret", response.getBody().getValue());
    }

    @Test
    void shouldCreateSecretAndReturnCreatedStatus() {
        SecretController controller = new SecretController(new SecretService(fakeSecretsManagerClient()));
        Secretdto request = new Secretdto("api-key", "abc123");

        ResponseEntity<Void> response = controller.createSecret(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldUpdateSecretAndReturnCreatedStatus() {
        SecretController controller = new SecretController(new SecretService(fakeSecretsManagerClient()));
        Secretdto request = new Secretdto("api-key", "updated-val");

        ResponseEntity<Void> response = controller.updateSecret(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldDeleteSecretAndReturnNoContent() {
        SecretController controller = new SecretController(new SecretService(fakeSecretsManagerClient()));

        ResponseEntity<Void> response = controller.deleteSecret("api-key");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    private static SecretsManagerClient fakeSecretsManagerClient() {
        Map<String, String> store = new HashMap<>();
        store.put("db-password", "super-secret");

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
