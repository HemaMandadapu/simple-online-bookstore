package com.example.bookstore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class AuthToken {

    @Id
    private String token;

    @ManyToOne(optional = false)
    private AppUser user;

    public AuthToken() {
    }

    public AuthToken(String token, AppUser user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public AppUser getUser() {
        return user;
    }
}
