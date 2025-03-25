package com.exchange.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class User {
    private final String username;
    private double realizedProfit;
    private final Map<String, Order> activeOrders;
    private final Map<String, Position> positions;
    private final List<Trade> executedTrades;

    public User(String username) {
        this.username = username;
        this.realizedProfit = 0.0;
        this.activeOrders = new ConcurrentHashMap<>();
        this.positions = new ConcurrentHashMap<>();
        this.executedTrades = new CopyOnWriteArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public double getRealizedProfit() {
        return realizedProfit;
    }

    public void addRealizedProfit(double amount) {
        this.realizedProfit += amount;
    }

    public Map<String, Order> getActiveOrders() {
        return activeOrders;
    }

    public Map<String, Position> getPositions() {
        return positions;
    }

    public List<Trade> getExecutedTrades() {
        return executedTrades;
    }

    public void addOrder(Order order) {
        activeOrders.put(order.getId(), order);
    }

    public boolean removeOrder(String orderId) {
        return activeOrders.remove(orderId) != null;
    }

    public void addTrade(Trade trade) {
        executedTrades.add(trade);
    }

    public void updatePosition(String symbol, int quantity, double price, boolean isBuy) {
        // Get or create position
        Position position = positions.computeIfAbsent(symbol, k -> new Position(symbol, 0, 0));
        int oldQty = position.getQuantity();
        double oldAvgCost = position.getAverageCost();
        
        if (isBuy) {
            // Buying increases position
            int newQty = oldQty + quantity;
            
            // If closing a short position (from negative to zero/positive)
            if (oldQty < 0 && newQty >= 0) {
                // Calculate closing quantity (the amount that closes the existing short)
                int closingQty = Math.min(quantity, -oldQty);
                
                // Calculate profit on the closed portion (for shorts: buy price - sell price)
                double profit = (oldAvgCost - price) * closingQty;
                addRealizedProfit(profit);
                
                // If we completely closed the position
                if (newQty == 0) {
                    positions.remove(symbol);
                } 
                // If we've flipped to a long position
                else if (newQty > 0) {
                    position.setQuantity(newQty);
                    position.setAverageCost(price);  // New cost basis for the long position
                }
            } 
            else {
                // Adding to existing long or reducing short
                if (newQty > 0) {
                    // Calculate new average cost for a long position
                    double totalCost = (oldQty * oldAvgCost) + (quantity * price);
                    position.setQuantity(newQty);
                    position.setAverageCost(totalCost / newQty);
                } else {
                    // Still short, just update the position size
                    position.setQuantity(newQty);
                }
            }
        } 
        else {  // Selling
            // Selling decreases position
            int newQty = oldQty - quantity;
            
            // If closing a long position (from positive to zero/negative)
            if (oldQty > 0 && newQty <= 0) {
                // Calculate closing quantity (the amount that closes the existing long)
                int closingQty = Math.min(quantity, oldQty);
                
                // Calculate profit on the closed portion (for longs: sell price - buy price)
                double profit = (price - oldAvgCost) * closingQty;
                addRealizedProfit(profit);
                
                // If we completely closed the position
                if (newQty == 0) {
                    positions.remove(symbol);
                } 
                // If we've flipped to a short position
                else if (newQty < 0) {
                    position.setQuantity(newQty);
                    position.setAverageCost(price);  // New cost basis for the short position
                }
            } 
            else {
                // Adding to existing short or reducing long
                if (newQty < 0) {
                    // Calculate new average cost for a short position
                    if (oldQty < 0) {
                        // Adding to existing short position
                        double totalCost = (Math.abs(oldQty) * oldAvgCost) + (quantity * price);
                        position.setQuantity(newQty);
                        position.setAverageCost(totalCost / Math.abs(newQty));
                    } else {
                        // We're creating a new short position
                        position.setQuantity(newQty);
                        position.setAverageCost(price);
                    }
                } else {
                    // Still long, just update the position size
                    position.setQuantity(newQty);
                }
            }
        }
        
        // If position quantity is zero, remove it
        if (position.getQuantity() == 0) {
            positions.remove(symbol);
        }
    }

    public List<Map<String, Object>> getPositionDetails() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Position position : positions.values()) {
            Map<String, Object> positionMap = new HashMap<>();
            positionMap.put("symbol", position.getSymbol());
            positionMap.put("quantity", position.getQuantity());
            positionMap.put("averageCost", position.getAverageCost());
            positionMap.put("positionType", position.getQuantity() > 0 ? "LONG" : "SHORT");
            result.add(positionMap);
        }
        
        return result;
    }

    public List<Map<String, Object>> getActiveOrdersDetails() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Order order : activeOrders.values()) {
            if (order.getStatus() == Order.OrderStatus.ACTIVE || 
                order.getStatus() == Order.OrderStatus.PARTIALLY_FILLED) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("orderId", order.getId());
                orderMap.put("book", order.getBook());
                orderMap.put("type", order.getType().toString());
                orderMap.put("price", order.getPrice());
                orderMap.put("quantity", order.getQuantity());
                orderMap.put("status", order.getStatus().toString());
                result.add(orderMap);
            }
        }
        
        return result;
    }

    public List<Map<String, Object>> getExecutedTradesDetails() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Trade trade : executedTrades) {
            Map<String, Object> tradeMap = new HashMap<>();
            tradeMap.put("tradeId", trade.getId());
            tradeMap.put("orderId", username.equals(trade.getBuyerUsername()) ? 
                         trade.getBuyOrderId() : trade.getSellOrderId());
            tradeMap.put("symbol", trade.getSymbol());
            tradeMap.put("price", trade.getPrice());
            tradeMap.put("quantity", trade.getQuantity());
            tradeMap.put("side", username.equals(trade.getBuyerUsername()) ? "BUY" : "SELL");
            tradeMap.put("timestamp", trade.getTimestamp().toString());
            tradeMap.put("counterparty", username.equals(trade.getBuyerUsername()) ? 
                         trade.getSellerUsername() : trade.getBuyerUsername());
            result.add(tradeMap);
        }
        
        return result;
    }
    
    // Simple summary for leaderboard
    public Map<String, Object> getLeaderboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("username", username);
        summary.put("realizedProfit", realizedProfit);
        summary.put("positionCount", positions.size());
        summary.put("activeOrderCount", activeOrders.size());
        summary.put("tradeCount", executedTrades.size());
        return summary;
    }
}