package com.example.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.example.bookstore.util.TestHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerTest extends BaseIntegrationTest {

    @Test
    void shouldRejectCartRequestWithoutAuthorizationHeader() throws Exception {
        mvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bearer token required"));
    }

    @Test
    void shouldRejectInvalidBearerToken() throws Exception {
        mvc.perform(get("/api/cart").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void shouldRejectAuthorizationHeaderWithoutBearerPrefix() throws Exception {
        mvc.perform(get("/api/cart").header("Authorization", "invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bearer token required"));
    }

    @Test
    void shouldReturnEmptyCartForNewUser() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        mvc.perform(get("/api/cart").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldAddBookToCart() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        mvc.perform(post("/api/cart")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.book.id").value(1))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void shouldIncreaseQuantityWhenSameBookIsAddedAgain() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        addBookToCart(mvc, json, token, 1L, 2);
        mvc.perform(post("/api/cart")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"quantity\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void shouldAllowDifferentBooksInCart() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        addBookToCart(mvc, json, token, 1L, 1);
        addBookToCart(mvc, json, token, 2L, 1);
        mvc.perform(get("/api/cart").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldRejectNonExistingBook() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        mvc.perform(post("/api/cart")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":9999,\"quantity\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found"));
    }

    @Test
    void shouldRejectZeroQuantityWhenAddingBook() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        mvc.perform(post("/api/cart")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Book id and positive quantity are required"));
    }

    @Test
    void shouldRejectNegativeQuantityWhenAddingBook() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        mvc.perform(post("/api/cart")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"quantity\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMissingBookId() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        mvc.perform(post("/api/cart")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMissingQuantity() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        mvc.perform(post("/api/cart")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateCartItemQuantity() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        Long id = addBookToCart(mvc, json, token, 1L, 1);
        mvc.perform(put("/api/cart/{id}", id)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void shouldRejectDifferentBookIdDuringCartUpdate() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        Long id = addBookToCart(mvc, json, token, 1L, 1);
        mvc.perform(put("/api/cart/{id}", id)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":2,\"quantity\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Invalid book id for the specified cart item"));
    }

    @Test
    void shouldRejectZeroQuantityDuringUpdate() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        Long id = addBookToCart(mvc, json, token, 1L, 1);
        mvc.perform(put("/api/cart/{id}", id)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quantity must be at least 1"));
    }

    @Test
    void shouldReturnNotFoundForUnknownCartItemDuringUpdate() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        mvc.perform(put("/api/cart/9999")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"quantity\":2}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cart item not found"));
    }

    @Test
    void shouldPreventUserFromUpdatingAnotherUsersCartItem() throws Exception {
        String first = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        String second = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        Long id = addBookToCart(mvc, json, first, 1L, 1);
        mvc.perform(put("/api/cart/{id}", id)
                        .header("Authorization", bearer(second))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"quantity\":3}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cart item not found"));
    }

    @Test
    void shouldRemoveCartItem() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        Long id = addBookToCart(mvc, json, token, 1L, 1);
        mvc.perform(delete("/api/cart/{id}", id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/cart").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownCartItem() throws Exception {
        String token = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        mvc.perform(delete("/api/cart/9999")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cart item not found"));
    }

    @Test
    void shouldPreventUserFromDeletingAnotherUsersCartItem() throws Exception {
        String first = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        String second = registerAndGetToken(mvc, json, uniqueUsername(), "pass123");
        Long id = addBookToCart(mvc, json, first, 1L, 1);
        mvc.perform(delete("/api/cart/{id}", id)
                        .header("Authorization", bearer(second)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cart item not found"));
    }
}
