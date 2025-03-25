package com.exchange.model;

import java.util.UUID;

public class Order {
    private final String id;
    private final String book;
    private final OrderType type;
    private final double price;
    private int quantity;
    private final String username;
    private OrderStatus status;
    private final long timestamp;
    private Double lastExecutionPrice; // Price at which the order was last executed
    
    public Order(String book, OrderType type, double price, int quantity, String username) {
        this.id = UUID.randomUUID().toString();
        this.book = book;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.username = username;
        this.status = OrderStatus.ACTIVE;
        this.timestamp = System.currentTimeMillis();
        this.lastExecutionPrice = null;
    }

    // Getters
    public String getId() { return id; }
    public String getBook() { return book; }
    public OrderType getType() { return type; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getUsername() { return username; }
    public OrderStatus getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
    public Double getLastExecutionPrice() { return lastExecutionPrice; }

    // Setters
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setLastExecutionPrice(Double lastExecutionPrice) { this.lastExecutionPrice = lastExecutionPrice; }

    // Enum for order types
    public enum OrderType {
        BUY, SELL
    }

    // Enum for order status
    public enum OrderStatus {
        ACTIVE, FILLED, PARTIALLY_FILLED, CANCELLED
    }
}