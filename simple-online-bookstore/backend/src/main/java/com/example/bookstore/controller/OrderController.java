package com.example.bookstore.controller;

import com.example.bookstore.model.AppUser;
import com.example.bookstore.model.CartItem;
import com.example.bookstore.model.CustomerOrder;
import com.example.bookstore.repository.CartItemRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orders;
    private final CartItemRepository cart;
    private final AuthService auth;

    public OrderController(
            OrderRepository orders,
            CartItemRepository cart,
            AuthService auth
    ) {
        this.orders = orders;
        this.cart = cart;
        this.auth = auth;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CustomerOrder> checkout(
            @RequestHeader(value = "Authorization", required = false) String h
    ) {
        AppUser u = auth.require(h);

        List<CartItem> items = cart.findByUserId(u.getId());

        if (items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cart is empty"
            );
        }

        CustomerOrder o = new CustomerOrder(u);

        items.forEach(item ->
                o.addItem(
                        item.getBook(),
                        item.getQuantity()
                )
        );

        CustomerOrder saved = orders.save(o);

        cart.deleteByUserId(u.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping
    public Object history(
            @RequestHeader(value = "Authorization", required = false) String h
    ) {
        return orders.findByUserIdOrderByCreatedAtDesc(
                auth.require(h).getId()
        );
    }
}
