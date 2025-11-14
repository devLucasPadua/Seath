package com.example.seath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement; 

@SpringBootApplication
@EnableTransactionManagement // <-- ADICIONE ESTA ANOTAÇÃO
@EnableJpaRepositories
public class SeathApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeathApplication.class, args);
    }
}
