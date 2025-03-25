// Main dashboard with 4 orderbook quadrants
import React, { useState, useEffect, useRef } from 'react';
import OrderbookQuadrant from './OrderbookQuadrant';
import { fetchAllOrderbooks } from '../services/api';

function Dashboard({ onViewOrderbook }) {
  const [orderbooks, setOrderbooks] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const prevDataRef = useRef(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        if (loading) {
          // Only show loading indicator on first load, not on refreshes
          setLoading(true);
        }
        
        const data = await fetchAllOrderbooks();
        
        // Only update state if data has changed to prevent unnecessary re-renders
        if (JSON.stringify(data) !== JSON.stringify(prevDataRef.current)) {
          prevDataRef.current = data;
          setOrderbooks(data);
        }
        
        setLoading(false);
      } catch (err) {
        setError('Failed to load orderbooks. Please try again later.');
        setLoading(false);
        console.error('Error fetching orderbooks:', err);
      }
    };

    fetchData();
    
    // Set up polling to refresh data every 5 seconds
    const intervalId = setInterval(fetchData, 5000);
    
    // Clean up interval on component unmount
    return () => clearInterval(intervalId);
  }, []);

  if (loading && !Object.keys(orderbooks).length) {
    return <div className="loading-message">Loading orderbooks...</div>;
  }
  
  if (error) return <div className="error-message">{error}</div>;

  // Get the array of book names
  const bookNames = Object.keys(orderbooks);

  return (
    <div className="dashboard">
      <h1 className="dashboard-title">Exchange Orderbooks</h1>
      
      <div className="orderbooks-grid">
        {bookNames.map((bookName) => (
          <OrderbookQuadrant 
            key={bookName}
            bookName={bookName} 
            bookData={orderbooks[bookName]}
            onViewFull={() => onViewOrderbook(bookName)}
          />
        ))}
      </div>
    </div>
  );
}

export default Dashboard;