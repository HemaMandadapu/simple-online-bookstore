package com.example.bookstore.config;

import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    CommandLineRunner seed(BookRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(
                    List.of(
                        new Book(
                            "Clean Code",
                            "Robert C. Martin",
                            new BigDecimal("35.00")
                        ),
                        new Book(
                            "Effective Java",
                            "Joshua Bloch",
                            new BigDecimal("42.50")
                        ),
                        new Book(
                            "Spring in Action",
                            "Craig Walls",
                            new BigDecimal("39.99")
                        )
                    )
                );
            }
        };
    }
}
