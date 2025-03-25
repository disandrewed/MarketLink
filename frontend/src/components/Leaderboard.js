// Component for displaying user leaderboard
import React, { useState, useEffect } from 'react';
import { fetchLeaderboard } from '../services/api';

function Leaderboard({ onBack }) {
  const [leaderboardData, setLeaderboardData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const data = await fetchLeaderboard();
        setLeaderboardData(data);
        setLoading(false);
      } catch (err) {
        setError('Failed to load leaderboard data. Please try again later.');
        setLoading(false);
        console.error('Error fetching leaderboard:', err);
      }
    };

    fetchData();
    
    // Refresh every 10 seconds
    const intervalId = setInterval(fetchData, 10000);
    return () => clearInterval(intervalId);
  }, []);

  return (
    <div className="leaderboard-container">
      <div className="leaderboard-header">
        <button className="back-button" onClick={onBack}>
          &larr; Back to Dashboard
        </button>
        <h1 className="leaderboard-title">Trader Leaderboard</h1>
      </div>

      {loading && <div className="loading-message">Loading leaderboard...</div>}
      {error && <div className="error-message">{error}</div>}

      {!loading && !error && (
        <div className="leaderboard-table-container">
          {leaderboardData.length > 0 ? (
            <table className="leaderboard-table">
              <thead>
                <tr>
                  <th>Rank</th>
                  <th>Username</th>
                  <th>Realized Profit</th>
                  <th>Positions</th>
                  <th>Active Orders</th>
                  <th>Total Trades</th>
                </tr>
              </thead>
              <tbody>
                {leaderboardData.map((trader, index) => (
                  <tr key={trader.username} className={index < 3 ? `rank-${index + 1}` : ''}>
                    <td className="rank-cell">
                      {index < 3 ? (
                        <span className={`trophy rank-${index + 1}`}>
                          {index === 0 ? '🥇' : index === 1 ? '🥈' : '🥉'}
                        </span>
                      ) : (
                        `#${index + 1}`
                      )}
                    </td>
                    <td>{trader.username}</td>
                    <td className={trader.realizedProfit >= 0 ? 'positive-profit' : 'negative-profit'}>
                      ${trader.realizedProfit.toFixed(2)}
                    </td>
                    <td>{trader.positionCount}</td>
                    <td>{trader.activeOrderCount}</td>
                    <td>{trader.tradeCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="no-data-message">
              No traders have made any trades yet. Be the first one to place an order!
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default Leaderboard;