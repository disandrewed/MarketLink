// Component for detailed view of a single orderbook
import React, { useState, useEffect, useRef } from 'react';
import { fetchOrderbook } from '../services/api';
import OrderbookDepthChart from './OrderbookDepthChart';

function FocusedOrderbook({ bookName, onBack }) {
  const [orderbookData, setOrderbookData] = useState(null);
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
        
        const data = await fetchOrderbook(bookName);
        
        // Only update state if data has changed to prevent unnecessary re-renders
        if (JSON.stringify(data) !== JSON.stringify(prevDataRef.current)) {
          prevDataRef.current = data;
          setOrderbookData(data);
        }
        
        setLoading(false);
      } catch (err) {
        setError(`Failed to load ${bookName} orderbook. Please try again.`);
        setLoading(false);
        console.error('Error fetching orderbook:', err);
      }
    };

    fetchData();
    
    // Set up polling to refresh data every 5 seconds
    const intervalId = setInterval(fetchData, 5000);
    
    // Clean up interval on component unmount
    return () => clearInterval(intervalId);
  }, [bookName, loading]);

  if (loading && !orderbookData) return <div className="loading-message">Loading {bookName} orderbook...</div>;
  if (error) return <div className="error-message">{error}</div>;

  // Sort sell levels in ascending order (lowest price at top)
  const sortedSellLevels = orderbookData?.sell ? 
    [...orderbookData.sell].sort((a, b) => a[0] - b[0]) : [];
  // Buy levels come pre-sorted from highest to lowest

  return (
    <div className="focused-orderbook">
      <div className="focused-header">
        <button className="back-button" onClick={onBack}>
          &larr; Back to Dashboard
        </button>
        <h1 className="focused-title">{bookName} Orderbook</h1>
      </div>

      <div className="full-orderbook">
        <div className="orderbook-column">
          <h2 className="column-title buy-title">Buy Orders</h2>
          <div className="price-levels-container">
            <div className="level-header">
              <span>Price</span>
              <span>Quantity</span>
            </div>
            <div className="scrollable-levels">
              {orderbookData?.buy?.length > 0 ? (
                orderbookData.buy.map((level, index) => (
                  <div key={`buy-${index}`} className="price-level buy-level">
                    <span className="price buy-price">${level[0].toFixed(2)}</span>
                    <span className="quantity">{level[1].toFixed(2)}</span>
                  </div>
                ))
              ) : (
                <div className="no-orders">No buy orders</div>
              )}
            </div>
          </div>
        </div>

        <div className="orderbook-column">
          <h2 className="column-title sell-title">Sell Orders</h2>
          <div className="price-levels-container">
            <div className="level-header">
              <span>Price</span>
              <span>Quantity</span>
            </div>
            <div className="scrollable-levels">
              {sortedSellLevels.length > 0 ? (
                sortedSellLevels.map((level, index) => (
                  <div key={`sell-${index}`} className="price-level sell-level">
                    <span className="price sell-price">${level[0].toFixed(2)}</span>
                    <span className="quantity">{level[1].toFixed(2)}</span>
                  </div>
                ))
              ) : (
                <div className="no-orders">No sell orders</div>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="depth-chart-container">
        <h2 className="depth-chart-title">Orderbook Depth Chart</h2>
        {orderbookData && (
          <OrderbookDepthChart buyLevels={orderbookData.buy} sellLevels={orderbookData.sell} />
        )}
      </div>
    </div>
  );
}

export default FocusedOrderbook;