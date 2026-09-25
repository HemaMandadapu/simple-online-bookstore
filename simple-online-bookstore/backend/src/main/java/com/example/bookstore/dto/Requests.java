package com.example.bookstore.dto;

public final class Requests {

    private Requests() {
    }

    public record Credentials(
            String username,
            String password
    ) {
    }

    public record TokenResponse(
            String token,
            String username
    ) {
    }

    public record CartRequest(
            Long bookId,
            Integer quantity
    ) {
    }

    public record ErrorResponse(
            String message
    ) {
    }
}