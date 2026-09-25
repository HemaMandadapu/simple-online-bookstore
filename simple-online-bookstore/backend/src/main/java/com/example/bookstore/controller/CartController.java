package com.example.bookstore.controller;

import com.example.bookstore.dto.Requests.CartRequest;
import com.example.bookstore.model.AppUser;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.CartItem;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartItemRepository;
import com.example.bookstore.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartItemRepository cart;
    private final BookRepository books;
    private final AuthService auth;

    public CartController(
            CartItemRepository cart,
            BookRepository books,
            AuthService auth
    ) {
        this.cart = cart;
        this.books = books;
        this.auth = auth;
    }

    @GetMapping
    public List<CartItem> get(
            @RequestHeader(value = "Authorization", required = false) String h
    ) {
        return cart.findByUserId(auth.require(h).getId());
    }

    @PostMapping
    public ResponseEntity<CartItem> add(
            @RequestHeader(value = "Authorization", required = false) String h,
            @RequestBody CartRequest r
    ) {
        AppUser u = auth.require(h);
        validate(r);

        Book b = books.findById(r.bookId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book not found"
                ));

        CartItem item = cart.findByUserIdAndBookId(u.getId(), b.getId())
                .orElse(new CartItem(u, b, 0));

        item.setQuantity(item.getQuantity() + r.quantity());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cart.save(item));
    }

    @PutMapping("/{id}")
    public CartItem update(
            @RequestHeader(value = "Authorization", required = false) String h,
            @PathVariable Long id,
            @RequestBody CartRequest r
    ) {
        AppUser u = auth.require(h);

        if (r == null
                || r.bookId() == null
                || r.quantity() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Book id and quantity are required"
            );
        }

        if (r.quantity() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be at least 1"
            );
        }

        CartItem item = owned(id, u);

        if (!item.getBook().getId().equals(r.bookId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid book id for the specified cart item"
            );
        }

        item.setQuantity(r.quantity());

        return cart.save(item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(
            @RequestHeader(value = "Authorization", required = false) String h,
            @PathVariable Long id
    ) {
        AppUser u = auth.require(h);

        cart.delete(owned(id, u));

        return ResponseEntity.noContent().build();
    }

    private CartItem owned(Long id, AppUser u) {
        return cart.findById(id)
                .filter(i -> i.getUser().getId().equals(u.getId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"
                ));
    }

    private void validate(CartRequest r) {
        if (r == null
                || r.bookId() == null
                || r.quantity() == null
                || r.quantity() < 1) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Book id and positive quantity are required"
            );
        }
    }
}
