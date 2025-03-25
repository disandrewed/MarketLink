package com.exchange.model;

public class Position {
    private final String symbol;
    private int quantity;
    private double averageCost;
    
    public Position(String symbol, int quantity) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageCost = 0.0;
    }
    
    public Position(String symbol, int quantity, double averageCost) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageCost = averageCost;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public double getAverageCost() {
        return averageCost;
    }
    
    public void setAverageCost(double averageCost) {
        this.averageCost = averageCost;
    }
    
    // Is this a long position?
    public boolean isLong() {
        return quantity > 0;
    }
    
    // Is this a short position?
    public boolean isShort() {
        return quantity < 0;
    }
    
    // Get the current market value based on a given current price
    public double getMarketValue(double currentPrice) {
        return quantity * currentPrice;
    }
    
    // Calculate unrealized profit/loss based on current price
    public double getUnrealizedPnL(double currentPrice) {
        if (isLong()) {
            return quantity * (currentPrice - averageCost);
        } else if (isShort()) {
            return Math.abs(quantity) * (averageCost - currentPrice);
        }
        return 0.0;
    }
}