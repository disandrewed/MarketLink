// Component for placing new orders
import React, { useState } from 'react';
import { placeOrder } from '../services/api';

function OrderForm({ onBack }) {
  const [formData, setFormData] = useState({
    username: '',
    book: 'AAPL',
    type: 'buy',
    price: '',
    quantity: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [orderResult, setOrderResult] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    
    // Additional validation for quantity field to ensure it's an integer
    if (name === 'quantity') {
      // Only allow integer values for quantity
      const intValue = value === '' ? '' : Math.floor(parseFloat(value));
      setFormData({
        ...formData,
        [name]: intValue.toString()
      });
      return;
    }
    
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate inputs
    if (!formData.username.trim()) {
      setError('Username is required');
      return;
    }
    
    const price = parseFloat(formData.price);
    if (isNaN(price) || price <= 0) {
      setError('Price must be a positive number');
      return;
    }
    
    const quantity = parseInt(formData.quantity, 10);
    if (isNaN(quantity) || quantity <= 0) {
      setError('Quantity must be a positive integer');
      return;
    }
    
    try {
      setLoading(true);
      setError(null);
      const result = await placeOrder(
        formData.book,
        formData.type,
        price,
        quantity,
        formData.username
      );
      
      setOrderResult(result);
      setLoading(false);
      
      // Reset form after successful order
      setFormData({
        username: formData.username, // Keep username for convenience
        book: formData.book, // Keep selected book
        type: 'buy',
        price: '',
        quantity: ''
      });
    } catch (err) {
      setError(err.message || 'Failed to place order. Please try again.');
      setOrderResult(null);
      setLoading(false);
      console.error('Error placing order:', err);
    }
  };

  return (
    <div className="order-form-container">
      <div className="form-header">
        <button className="back-button" onClick={onBack}>
          &larr; Back to Dashboard
        </button>
        <h1 className="form-title">Place New Order</h1>
      </div>

      {orderResult && (
        <div className="order-success">
          <h3>Order Placed Successfully!</h3>
          <p>Order ID: <span className="order-id">{orderResult.orderId}</span></p>
          <p>Status: <span className="order-status">{orderResult.status}</span></p>
          <button 
            className="dismiss-button" 
            onClick={() => setOrderResult(null)}
          >
            Place Another Order
          </button>
        </div>
      )}

      {!orderResult && (
        <form className="order-form" onSubmit={handleSubmit}>
          {error && <div className="error-message">{error}</div>}
          
          <div className="form-group">
            <label htmlFor="username">Username:</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Enter your username"
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="book">Select Orderbook:</label>
            <select
              id="book"
              name="book"
              value={formData.book}
              onChange={handleChange}
              required
            >
              <option value="AAPL">AAPL</option>
              <option value="AMZN">AMZN</option>
              <option value="NVDA">NVDA</option>
              <option value="MSFT">MSFT</option>
            </select>
          </div>
          
          <div className="form-group">
            <label htmlFor="type">Order Type:</label>
            <select
              id="type"
              name="type"
              value={formData.type}
              onChange={handleChange}
              required
            >
              <option value="buy">Buy</option>
              <option value="sell">Sell</option>
            </select>
          </div>
          
          <div className="form-group">
            <label htmlFor="price">Price:</label>
            <input
              type="number"
              id="price"
              name="price"
              value={formData.price}
              onChange={handleChange}
              placeholder="Enter price"
              step="0.01"
              min="0.01"
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="quantity">Quantity (integers only):</label>
            <input
              type="number"
              id="quantity"
              name="quantity"
              value={formData.quantity}
              onChange={handleChange}
              placeholder="Enter quantity"
              step="1"
              min="1"
              pattern="\d*"
              onInvalid={(e) => e.target.setCustomValidity('Please enter a positive integer')}
              onInput={(e) => e.target.setCustomValidity('')}
              required
            />
            <small className="form-text text-muted">Quantity must be a whole number</small>
          </div>
          
          <button 
            type="submit" 
            className="submit-button" 
            disabled={loading}
          >
            {loading ? 'Placing Order...' : 'Place Order'}
          </button>
        </form>
      )}
    </div>
  );
}

export default OrderForm;