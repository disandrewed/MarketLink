// Component for searching and displaying user data
import React, { useState } from 'react';
import { fetchUser } from '../services/api';

function UserSearch({ onBack }) {
  const [username, setUsername] = useState('');
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!username.trim()) {
      setError('Please enter a username');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await fetchUser(username);
      setUserData(data);
      setLoading(false);
    } catch (err) {
      setError(err.message || 'Failed to find user. Please check the username and try again.');
      setUserData(null);
      setLoading(false);
      console.error('Error fetching user:', err);
    }
  };

  return (
    <div className="user-search-container">
      <div className="search-header">
        <button className="back-button" onClick={onBack}>
          &larr; Back to Dashboard
        </button>
        <h1 className="search-title">User Search</h1>
      </div>

      <form className="search-form" onSubmit={handleSearch}>
        <input
          type="text"
          className="search-input"
          placeholder="Enter username..."
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <button type="submit" className="search-button" disabled={loading}>
          {loading ? 'Searching...' : 'Search'}
        </button>
      </form>

      {error && <div className="error-message">{error}</div>}

      {userData && (
        <div className="user-data">
          <div className="user-info">
            <h2 className="username">{userData.username}</h2>
            <p className="profit">
              Realized Profit: <span className="profit-value">${userData.realizedProfit.toFixed(2)}</span>
            </p>
          </div>

          <h3 className="positions-title">Current Positions</h3>
          {userData.positions.length > 0 ? (
            <div className="positions-table-container">
              <table className="positions-table">
                <thead>
                  <tr>
                    <th>Symbol</th>
                    <th>Quantity</th>
                    <th>Average Cost</th>
                    <th>Position Type</th>
                  </tr>
                </thead>
                <tbody>
                  {userData.positions.map((position, index) => (
                    <tr key={`position-${index}`}>
                      <td>{position.symbol}</td>
                      <td>{Math.abs(position.quantity).toFixed(2)}</td>
                      <td>${position.averageCost.toFixed(2)}</td>
                      <td className={position.positionType === 'LONG' ? 'long-position' : 'short-position'}>
                        {position.positionType}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="no-positions-message">No positions</p>
          )}

          <h3 className="orders-title">Active Orders</h3>
          {userData.activeOrders.length > 0 ? (
            <div className="orders-table-container">
              <table className="orders-table">
                <thead>
                  <tr>
                    <th>Order ID</th>
                    <th>Symbol</th>
                    <th>Type</th>
                    <th>Price</th>
                    <th>Quantity</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {userData.activeOrders.map((order) => (
                    <tr key={order.orderId}>
                      <td>{order.orderId.substring(0, 8)}...</td>
                      <td>{order.book}</td>
                      <td className={order.type === 'BUY' ? 'buy-type' : 'sell-type'}>
                        {order.type}
                      </td>
                      <td>${order.price.toFixed(2)}</td>
                      <td>{order.quantity.toFixed(2)}</td>
                      <td>{order.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="no-orders-message">No active orders</p>
          )}

          <h3 className="trades-title">Executed Trades</h3>
          {userData.executedTrades && userData.executedTrades.length > 0 ? (
            <div className="trades-table-container">
              <table className="trades-table">
                <thead>
                  <tr>
                    <th>Trade ID</th>
                    <th>Order ID</th>
                    <th>Symbol</th>
                    <th>Side</th>
                    <th>Price</th>
                    <th>Quantity</th>
                    <th>Timestamp</th>
                    <th>Counterparty</th>
                  </tr>
                </thead>
                <tbody>
                  {userData.executedTrades.map((trade) => (
                    <tr key={trade.tradeId}>
                      <td>{trade.tradeId.substring(0, 8)}...</td>
                      <td>{trade.orderId.substring(0, 8)}...</td>
                      <td>{trade.symbol}</td>
                      <td className={trade.side === 'BUY' ? 'buy-type' : 'sell-type'}>
                        {trade.side}
                      </td>
                      <td>${trade.price.toFixed(2)}</td>
                      <td>{trade.quantity.toFixed(2)}</td>
                      <td>{new Date(trade.timestamp).toLocaleString()}</td>
                      <td>{trade.counterparty}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="no-trades-message">No executed trades</p>
          )}
        </div>
      )}
    </div>
  );
}

export default UserSearch;