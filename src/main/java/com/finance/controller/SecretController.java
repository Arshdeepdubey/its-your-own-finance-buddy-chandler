package com.finance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.Secretdto;
import com.finance.service.SecretService;




@RestController
@RequestMapping("/api/v1")
public class SecretController {
    
    private final SecretService secretService;

    public SecretController(SecretService secretService) {
        this.secretService = secretService;
    }

    @GetMapping("/getSecret")
    public ResponseEntity<List<Secretdto>> getSecrets() {
        return ResponseEntity.ok(secretService.getSecrets());
    }

    @PostMapping("/createSecret")
    public ResponseEntity<Void> createSecret(@RequestBody Secretdto dto) {
        secretService.createSecret(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/updateSecret")
    public ResponseEntity<Void> updateSecret(@RequestBody Secretdto dto) {
        secretService.updateSecret(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteSecret(@PathVariable String name){
        secretService.deleteSecret(name);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleAwsConfigurationError(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", ex.getMessage()));
    }
    
}
