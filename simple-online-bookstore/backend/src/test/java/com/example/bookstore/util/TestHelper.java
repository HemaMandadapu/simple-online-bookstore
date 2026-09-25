package com.example.bookstore.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class TestHelper {

    private TestHelper() {
    }

    public static String uniqueUsername() {
        return "user-" + UUID.randomUUID();
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }

    public static String registerAndGetToken(
            MockMvc mvc,
            ObjectMapper json,
            String username,
            String password
    ) throws Exception {
        String response = mvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(username, password))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return json.readTree(response)
                .get("token")
                .asText();
    }

    public static Long addBookToCart(
            MockMvc mvc,
            ObjectMapper json,
            String token,
            Long bookId,
            int quantity
    ) throws Exception {
        String response = mvc.perform(
                        post("/api/cart")
                                .header("Authorization", bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "bookId": %d,
                                          "quantity": %d
                                        }
                                        """.formatted(bookId, quantity))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode cartItem = json.readTree(response);
        return cartItem.get("id").asLong();
    }
}
