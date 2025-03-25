package com.exchange.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Trade {
    private final String id;
    private final String buyOrderId;
    private final String sellOrderId;
    private final String symbol;
    private final int quantity;
    private final double price;
    private final LocalDateTime timestamp;
    private final String buyerUsername;
    private final String sellerUsername;
    
    public Trade(String buyOrderId, String sellOrderId, String symbol, 
                 int quantity, double price, LocalDateTime timestamp,
                 String buyerUsername, String sellerUsername) {
        this.id = UUID.randomUUID().toString();
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
        this.buyerUsername = buyerUsername;
        this.sellerUsername = sellerUsername;
    }
    
    public String getId() {
        return id;
    }
    
    public String getBuyOrderId() {
        return buyOrderId;
    }
    
    public String getSellOrderId() {
        return sellOrderId;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getPrice() {
        return price;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getBuyerUsername() {
        return buyerUsername;
    }
    
    public String getSellerUsername() {
        return sellerUsername;
    }
    
    // Calculate the total value of this trade
    public double getTotalValue() {
        return quantity * price;
    }
}