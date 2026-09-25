package com.example.bookstore;

import org.junit.jupiter.api.Test;

import static com.example.bookstore.util.TestHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends BaseIntegrationTest {

    @Test
    void shouldCheckoutSuccessfully() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        addBookToCart(mvc, json, token, 1L, 2);
        mvc.perform(post("/api/orders").header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.total").value(70.0))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(35.0))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void shouldCalculateTotalForMultipleBooks() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        addBookToCart(mvc, json, token, 1L, 2);
        addBookToCart(mvc, json, token, 2L, 1);
        mvc.perform(post("/api/orders").header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(112.5))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void shouldClearCartAfterCheckout() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        addBookToCart(mvc, json, token, 1L, 2);
        mvc.perform(post("/api/orders").header("Authorization", bearer(token)))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/cart").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldRejectCheckoutWhenCartIsEmpty() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        mvc.perform(post("/api/orders").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cart is empty"));
    }

    @Test
    void shouldRejectCheckoutWithoutToken() throws Exception {
        mvc.perform(post("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bearer token required"));
    }

    @Test
    void shouldReturnEmptyOrderHistoryForNewUser() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        mvc.perform(get("/api/orders").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnPlacedOrderInHistory() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        addBookToCart(mvc, json, token, 1L, 2);
        mvc.perform(post("/api/orders").header("Authorization", bearer(token)))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/orders").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].total").value(70.0))
                .andExpect(jsonPath("$[0].items.length()").value(1));
    }

    @Test
    void shouldReturnOnlyAuthenticatedUsersOrders() throws Exception {
        String first = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        String second = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        addBookToCart(mvc, json, first, 1L, 1);
        mvc.perform(post("/api/orders").header("Authorization", bearer(first)))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/orders").header("Authorization", bearer(second)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldCompleteRegisterCartCheckoutAndHistoryFlow() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        Long firstId = addBookToCart(mvc, json, token, 1L, 1);
        addBookToCart(mvc, json, token, 2L, 2);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/cart/{id}", firstId)
                        .header("Authorization", bearer(token))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3));

        mvc.perform(post("/api/orders").header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(190.0))
                .andExpect(jsonPath("$.items.length()").value(2));

        mvc.perform(get("/api/cart").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mvc.perform(get("/api/orders").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].total").value(190.0));
    }
}
