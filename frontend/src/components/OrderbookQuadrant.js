// Component for a single orderbook quadrant
import React from 'react';
import OrderbookDepthChart from './OrderbookDepthChart';

function OrderbookQuadrant({ bookName, bookData, onViewFull }) {
  // Limit to 10 levels max per side
  const buyLevels = bookData?.buy?.slice(0, 10) || [];
  const sellLevels = bookData?.sell?.slice(0, 10) || [];
  
  // Sort sell levels to show lowest price at top (ascending)
  const sortedSellLevels = [...sellLevels].sort((a, b) => a[0] - b[0]);

  return (
    <div className="orderbook-quadrant">
      <div className="orderbook-header">
        <h2 className="orderbook-title">{bookName}</h2>
        <button className="view-full-button" onClick={onViewFull}>
          View Full Book
        </button>
      </div>

      <div className="orderbook-content">
        <div className="orderbook-side">
          <h3 className="side-title buy-title">Buy Orders</h3>
          <div className="price-levels">
            <div className="level-header">
              <span>Price</span>
              <span>Quantity</span>
            </div>
            {buyLevels.length > 0 ? (
              buyLevels.map((level, index) => (
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

        <div className="orderbook-side">
          <h3 className="side-title sell-title">Sell Orders</h3>
          <div className="price-levels">
            <div className="level-header">
              <span>Price</span>
              <span>Quantity</span>
            </div>
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

      <div className="quadrant-depth-chart">
        {(buyLevels.length > 0 || sellLevels.length > 0) && (
          <OrderbookDepthChart buyLevels={buyLevels} sellLevels={sellLevels} compact={true} />
        )}
      </div>
    </div>
  );
}

export default OrderbookQuadrant;