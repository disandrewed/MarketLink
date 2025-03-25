package com.exchange.service;

import com.exchange.model.Order;
import com.exchange.model.OrderBook;
import com.exchange.model.Trade;
import com.exchange.model.User;
import com.exchange.model.Position;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ExchangeService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeService.class);
    
    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Order> allOrders = new ConcurrentHashMap<>();
    private final List<Trade> allTrades = Collections.synchronizedList(new ArrayList<>());
    
    @PostConstruct
    public void init() {
        // Initialize the 4 required orderbooks
        orderBooks.put("AAPL", new OrderBook("AAPL"));
        orderBooks.put("AMZN", new OrderBook("AMZN"));
        orderBooks.put("NVDA", new OrderBook("NVDA")); // Changed from CLDE
        orderBooks.put("MSFT", new OrderBook("MSFT")); // Changed from OPAI
        
        logger.info("Exchange initialized with 4 orderbooks: AAPL, AMZN, NVDA, MSFT");
    }
    
    public Map<String, Object> placeOrder(String book, String orderType, double price, double quantity, String username) {
        // Validate inputs
        if (!orderBooks.containsKey(book)) {
            throw new IllegalArgumentException("Invalid orderbook: " + book);
        }
        
        // Ensure quantity is a positive integer by flooring and checking > 0
        int intQuantity = (int)Math.floor(quantity);
        if (intQuantity <= 0 || price <= 0) {
            throw new IllegalArgumentException("Price must be positive and quantity must be a positive integer");
        }
        
        // Get or create user
        User user = users.computeIfAbsent(username, User::new);
        
        // Create the order
        Order.OrderType type = "buy".equalsIgnoreCase(orderType) ? 
            Order.OrderType.BUY : Order.OrderType.SELL;
        
        Order order = new Order(book, type, price, intQuantity, username);
        
        // Store the order
        allOrders.put(order.getId(), order);
        user.addOrder(order);
        
        // Process the order (matching)
        processOrder(order);
        
        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("status", order.getStatus().toString());
        response.put("orderId", order.getId());
        
        return response;
    }
    
    private void processOrder(Order order) {
        OrderBook book = orderBooks.get(order.getBook());
        
        if (order.getType() == Order.OrderType.BUY) {
            // Match against sell orders
            processBuyOrder(order, book);
        } else {
            // Match against buy orders
            processSellOrder(order, book);
        }
        
        // If order is still active and has quantity, add it to the orderbook
        if ((order.getStatus() == Order.OrderStatus.ACTIVE || 
             order.getStatus() == Order.OrderStatus.PARTIALLY_FILLED) && 
            order.getQuantity() > 0) {
            book.addOrder(order);
        }
    }
    
    private void processBuyOrder(Order buyOrder, OrderBook book) {
        // Try to match against sell orders (lowest price first)
        Iterator<Map.Entry<Double, LinkedList<Order>>> sellOrdersIterator = 
            book.getSellOrders().entrySet().iterator();
        
        while (sellOrdersIterator.hasNext() && buyOrder.getQuantity() > 0) {
            Map.Entry<Double, LinkedList<Order>> priceLevel = sellOrdersIterator.next();
            
            // Only match if buy price >= sell price
            if (buyOrder.getPrice() < priceLevel.getKey()) {
                break;
            }
            
            LinkedList<Order> ordersAtPrice = priceLevel.getValue();
            Iterator<Order> orderIterator = ordersAtPrice.iterator();
            
            while (orderIterator.hasNext() && buyOrder.getQuantity() > 0) {
                Order sellOrder = orderIterator.next();
                
                // Match the orders
                executeMatch(buyOrder, sellOrder);
                
                // Remove filled sell order
                if (sellOrder.getQuantity() == 0) {
                    orderIterator.remove();
                    User seller = users.get(sellOrder.getUsername());
                    seller.removeOrder(sellOrder.getId());
                    sellOrder.setStatus(Order.OrderStatus.FILLED);
                } else {
                    sellOrder.setStatus(Order.OrderStatus.PARTIALLY_FILLED);
                }
            }
            
            // Remove empty price level
            if (ordersAtPrice.isEmpty()) {
                sellOrdersIterator.remove();
            }
        }
        
        // Update buy order status
        if (buyOrder.getQuantity() == 0) {
            buyOrder.setStatus(Order.OrderStatus.FILLED);
            User buyer = users.get(buyOrder.getUsername());
            buyer.removeOrder(buyOrder.getId());
        } else if (buyOrder.getStatus() != Order.OrderStatus.ACTIVE) {
            buyOrder.setStatus(Order.OrderStatus.PARTIALLY_FILLED);
        }
    }
    
    private void processSellOrder(Order sellOrder, OrderBook book) {
        // Try to match against buy orders (highest price first)
        Iterator<Map.Entry<Double, LinkedList<Order>>> buyOrdersIterator = 
            book.getBuyOrders().entrySet().iterator();
        
        while (buyOrdersIterator.hasNext() && sellOrder.getQuantity() > 0) {
            Map.Entry<Double, LinkedList<Order>> priceLevel = buyOrdersIterator.next();
            
            // Only match if sell price <= buy price
            if (sellOrder.getPrice() > priceLevel.getKey()) {
                break;
            }
            
            LinkedList<Order> ordersAtPrice = priceLevel.getValue();
            Iterator<Order> orderIterator = ordersAtPrice.iterator();
            
            while (orderIterator.hasNext() && sellOrder.getQuantity() > 0) {
                Order buyOrder = orderIterator.next();
                
                // Match the orders
                executeMatch(buyOrder, sellOrder);
                
                // Remove filled buy order
                if (buyOrder.getQuantity() == 0) {
                    orderIterator.remove();
                    User buyer = users.get(buyOrder.getUsername());
                    buyer.removeOrder(buyOrder.getId());
                    buyOrder.setStatus(Order.OrderStatus.FILLED);
                } else {
                    buyOrder.setStatus(Order.OrderStatus.PARTIALLY_FILLED);
                }
            }
            
            // Remove empty price level
            if (ordersAtPrice.isEmpty()) {
                buyOrdersIterator.remove();
            }
        }
        
        // Update sell order status
        if (sellOrder.getQuantity() == 0) {
            sellOrder.setStatus(Order.OrderStatus.FILLED);
            User seller = users.get(sellOrder.getUsername());
            seller.removeOrder(sellOrder.getId());
        } else if (sellOrder.getStatus() != Order.OrderStatus.ACTIVE) {
            sellOrder.setStatus(Order.OrderStatus.PARTIALLY_FILLED);
        }
    }
    
    private void executeMatch(Order buyOrder, Order sellOrder) {
        // Determine match quantity
        int matchQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
        
        // Execute at the price of the resting order
        double executionPrice = sellOrder.getTimestamp() < buyOrder.getTimestamp() ? 
            sellOrder.getPrice() : buyOrder.getPrice();
        
        // Update quantities
        buyOrder.setQuantity(buyOrder.getQuantity() - matchQuantity);
        sellOrder.setQuantity(sellOrder.getQuantity() - matchQuantity);
        
        // Record execution price
        buyOrder.setLastExecutionPrice(executionPrice);
        sellOrder.setLastExecutionPrice(executionPrice);
        
        // Get users
        User buyer = users.get(buyOrder.getUsername());
        User seller = users.get(sellOrder.getUsername());

        // Update user positions and calculate realized profit
        buyer.updatePosition(buyOrder.getBook(), matchQuantity, executionPrice, true);
        seller.updatePosition(sellOrder.getBook(), matchQuantity, executionPrice, false);
        
        // Create a trade record
        Trade trade = new Trade(
            buyOrder.getId(),
            sellOrder.getId(),
            buyOrder.getBook(),
            matchQuantity,
            executionPrice,
            LocalDateTime.now(),
            buyOrder.getUsername(),
            sellOrder.getUsername()
        );
        
        // Store the trade
        allTrades.add(trade);
        buyer.addTrade(trade);
        seller.addTrade(trade);
        
        logger.info("Match executed: {} {} @ ${} - Buyer: {}, Seller: {}", 
            matchQuantity, buyOrder.getBook(), executionPrice, 
            buyOrder.getUsername(), sellOrder.getUsername());
    }
    
    public Map<String, String> cancelOrder(String book, String orderId, String username) {
        // Validate inputs
        if (!orderBooks.containsKey(book)) {
            throw new IllegalArgumentException("Invalid orderbook: " + book);
        }
        
        Order order = allOrders.get(orderId);
        
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        if (!order.getUsername().equals(username)) {
            throw new IllegalArgumentException("Order does not belong to user: " + username);
        }
        
        if (order.getStatus() != Order.OrderStatus.ACTIVE && 
            order.getStatus() != Order.OrderStatus.PARTIALLY_FILLED) {
            throw new IllegalArgumentException("Order cannot be cancelled: " + order.getStatus());
        }
        
        // Remove from orderbook
        OrderBook orderBook = orderBooks.get(book);
        boolean removed = orderBook.removeOrder(orderId);
        
        // Update order status
        if (removed) {
            order.setStatus(Order.OrderStatus.CANCELLED);
            
            // Remove from user's active orders
            User user = users.get(username);
            user.removeOrder(orderId);
            
            logger.info("Order cancelled: {} by user {}", orderId, username);
        }
        
        // Return status
        Map<String, String> response = new HashMap<>();
        response.put("status", removed ? "CANCELLED" : "FAILED");
        
        return response;
    }
    
    public Map<String, Object> cancelAllOrders(String book, String username) {
        // Validate inputs
        if (!orderBooks.containsKey(book)) {
            throw new IllegalArgumentException("Invalid orderbook: " + book);
        }
        
        User user = users.get(username);
        
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        
        // Get all orders for this user and book
        List<String> cancelledOrders = new ArrayList<>();
        
        for (Order order : new ArrayList<>(user.getActiveOrders().values())) {
            if (order.getBook().equals(book) && 
                (order.getStatus() == Order.OrderStatus.ACTIVE || 
                 order.getStatus() == Order.OrderStatus.PARTIALLY_FILLED)) {
                
                // Cancel the order
                order.setStatus(Order.OrderStatus.CANCELLED);
                cancelledOrders.add(order.getId());
            }
        }
        
        // Remove from orderbook
        OrderBook orderBook = orderBooks.get(book);
        int count = orderBook.removeAllOrdersForUser(username);
        
        // Remove from user's active orders
        for (String orderId : cancelledOrders) {
            user.removeOrder(orderId);
        }
        
        logger.info("Cancelled {} orders for user {} in book {}", count, username, book);
        
        // Return status
        Map<String, Object> response = new HashMap<>();
        response.put("status", "CANCELLED");
        response.put("count", count);
        
        return response;
    }
    
    public Map<String, List<List<Double>>> getOrderBook(String book) {
        // Validate inputs
        if (!orderBooks.containsKey(book)) {
            throw new IllegalArgumentException("Invalid orderbook: " + book);
        }
        
        return orderBooks.get(book).getAggregatedView();
    }
    
    public Map<String, Map<String, List<List<Double>>>> getAllOrderBooks() {
        Map<String, Map<String, List<List<Double>>>> result = new HashMap<>();
        
        for (OrderBook book : orderBooks.values()) {
            result.put(book.getName(), book.getAggregatedView());
        }
        
        return result;
    }
    
    public Map<String, Object> getUser(String username) {
        User user = users.get(username);
        
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("realizedProfit", user.getRealizedProfit());
        result.put("positions", user.getPositionDetails());
        result.put("activeOrders", user.getActiveOrdersDetails());
        result.put("executedTrades", user.getExecutedTradesDetails());
        
        return result;
    }
    
    public List<Map<String, Object>> getLeaderboard() {
        // Sort users by realized profit in descending order
        return users.values().stream()
            .map(User::getLeaderboardSummary)
            .sorted((u1, u2) -> Double.compare(
                (double) u2.get("realizedProfit"), 
                (double) u1.get("realizedProfit")
            ))
            .collect(Collectors.toList());
    }
}