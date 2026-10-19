package org.example.oct19;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Imperative Spring Boot entry point for the common-management API. */
@SpringBootApplication
public class Oct19Application {
    /** Starts the HTTP application; no credentials or business records are seeded. */
    public static void main(String[] args) {
        SpringApplication.run(Oct19Application.class, args);
    }
}
