package com.example.bookstore.service;

import com.example.bookstore.dto.Requests.Credentials;
import com.example.bookstore.dto.Requests.TokenResponse;
import com.example.bookstore.model.AppUser;
import com.example.bookstore.model.AuthToken;
import com.example.bookstore.repository.AuthTokenRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository users;
    private final AuthTokenRepository tokens;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public AuthService(UserRepository users, AuthTokenRepository tokens) {
        this.users = users;
        this.tokens = tokens;
    }

    public TokenResponse register(Credentials credentials) {
        validate(credentials);

        if (users.findByUsername(credentials.username().trim()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username already exists"
            );
        }

        AppUser user = new AppUser(
                credentials.username().trim(),
                encoder.encode(credentials.password())
        );

        return issue(users.save(user));
    }

    public TokenResponse login(Credentials credentials) {
        validate(credentials);

        AppUser user = users
                .findByUsername(credentials.username().trim())
                .filter(existingUser ->
                        encoder.matches(
                                credentials.password(),
                                existingUser.getPasswordHash()
                        )
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid username or password"
                ));

        return issue(user);
    }

    public AppUser require(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Bearer token required"
            );
        }

        String token = header.substring(7);

        return tokens.findById(token)
                .map(AuthToken::getUser)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid token"
                ));
    }

    private TokenResponse issue(AppUser user) {
        String token = UUID.randomUUID().toString();

        tokens.save(new AuthToken(token, user));

        return new TokenResponse(
                token,
                user.getUsername()
        );
    }

    private void validate(Credentials credentials) {
        if (credentials == null
                || credentials.username() == null
                || credentials.username().isBlank()
                || credentials.password() == null
                || credentials.password().length() < 4) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username is required and password must have at least 4 characters"
            );
        }
    }
}
