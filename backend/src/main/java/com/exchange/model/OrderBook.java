package com.exchange.model;

import java.util.*;

public class OrderBook {
    private final String name;
    private final Map<Double, LinkedList<Order>> buyOrders;
    private final Map<Double, LinkedList<Order>> sellOrders;

    public OrderBook(String name) {
        this.name = name;
        // TreeMap to keep buy orders sorted by price (descending)
        this.buyOrders = new TreeMap<>(Collections.reverseOrder());
        // TreeMap to keep sell orders sorted by price (ascending)
        this.sellOrders = new TreeMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<Double, LinkedList<Order>> getBuyOrders() {
        return buyOrders;
    }

    public Map<Double, LinkedList<Order>> getSellOrders() {
        return sellOrders;
    }

    public void addOrder(Order order) {
        if (order.getType() == Order.OrderType.BUY) {
            buyOrders.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        } else {
            sellOrders.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).add(order);
        }
    }

    public boolean removeOrder(String orderId) {
        // Check buy orders
        for (LinkedList<Order> orders : buyOrders.values()) {
            for (Iterator<Order> it = orders.iterator(); it.hasNext();) {
                Order order = it.next();
                if (order.getId().equals(orderId)) {
                    it.remove();
                    return true;
                }
            }
        }

        // Check sell orders
        for (LinkedList<Order> orders : sellOrders.values()) {
            for (Iterator<Order> it = orders.iterator(); it.hasNext();) {
                Order order = it.next();
                if (order.getId().equals(orderId)) {
                    it.remove();
                    return true;
                }
            }
        }
        
        return false;
    }

    public int removeAllOrdersForUser(String username) {
        int count = 0;
        
        // Remove from buy orders
        for (LinkedList<Order> orders : buyOrders.values()) {
            for (Iterator<Order> it = orders.iterator(); it.hasNext();) {
                Order order = it.next();
                if (order.getUsername().equals(username)) {
                    it.remove();
                    count++;
                }
            }
        }
        
        // Remove from sell orders
        for (LinkedList<Order> orders : sellOrders.values()) {
            for (Iterator<Order> it = orders.iterator(); it.hasNext();) {
                Order order = it.next();
                if (order.getUsername().equals(username)) {
                    it.remove();
                    count++;
                }
            }
        }
        
        // Clean up empty price levels
        buyOrders.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        sellOrders.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        return count;
    }

    // Get aggregated book view
    public Map<String, List<List<Double>>> getAggregatedView() {
        Map<String, List<List<Double>>> result = new HashMap<>();
        
        List<List<Double>> buyList = new ArrayList<>();
        for (Map.Entry<Double, LinkedList<Order>> entry : buyOrders.entrySet()) {
            int totalQuantity = 0;
            for (Order order : entry.getValue()) {
                totalQuantity += order.getQuantity();
            }
            if (totalQuantity > 0) {
                buyList.add(Arrays.asList(entry.getKey(), (double) totalQuantity));
            }
        }
        
        List<List<Double>> sellList = new ArrayList<>();
        for (Map.Entry<Double, LinkedList<Order>> entry : sellOrders.entrySet()) {
            int totalQuantity = 0;
            for (Order order : entry.getValue()) {
                totalQuantity += order.getQuantity();
            }
            if (totalQuantity > 0) {
                sellList.add(Arrays.asList(entry.getKey(), (double) totalQuantity));
            }
        }
        
        result.put("buy", buyList);
        result.put("sell", sellList);
        
        return result;
    }
}