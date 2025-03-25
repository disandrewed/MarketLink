package com.exchange.controller;

import com.exchange.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange")
@CrossOrigin(origins = "*") // Allow calls from React frontend
public class ExchangeController {
    
    private final ExchangeService exchangeService;
    
    @Autowired
    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }
    
    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> placeOrder(
            @RequestParam String book,
            @RequestParam String type, // buy or sell
            @RequestParam double price,
            @RequestParam double quantity,
            @RequestParam String username) {
        
        try {
            Map<String, Object> result = exchangeService.placeOrder(book, type, price, quantity, username);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/order")
    public ResponseEntity<Map<String, String>> cancelOrder(
            @RequestParam String book,
            @RequestParam String orderId,
            @RequestParam String username) {
        
        try {
            Map<String, String> result = exchangeService.cancelOrder(book, orderId, username);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/orders")
    public ResponseEntity<Map<String, Object>> cancelAllOrders(
            @RequestParam String book,
            @RequestParam String username) {
        
        try {
            Map<String, Object> result = exchangeService.cancelAllOrders(book, username);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/book/{name}")
    public ResponseEntity<Map<String, List<List<Double>>>> getOrderBook(@PathVariable String name) {
        try {
            Map<String, List<List<Double>>> result = exchangeService.getOrderBook(name);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/books")
    public ResponseEntity<Map<String, Map<String, List<List<Double>>>>> getAllOrderBooks() {
        return ResponseEntity.ok(exchangeService.getAllOrderBooks());
    }
    
    @GetMapping("/user/{username}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable String username) {
        try {
            Map<String, Object> result = exchangeService.getUser(username);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        List<Map<String, Object>> leaderboard = exchangeService.getLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }
}