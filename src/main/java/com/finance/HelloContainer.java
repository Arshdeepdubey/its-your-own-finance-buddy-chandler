package com.finance;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloContainer {

        @GetMapping("/")
        public String index() {
            return "Greetings from Spring Boot! ";
        }
        
}
