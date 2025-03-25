// Top navigation bar component
import React from 'react';

function Navbar({ onDashboardClick, onUserSearchClick, onPlaceOrderClick, onLeaderboardClick }) {
  return (
    <nav className="navbar">
      <div className="navbar-brand-container">
        <div className="navbar-brand" onClick={onDashboardClick}>MarketLink</div>
        <div className="navbar-byline">by disandrewed</div>
      </div>
      <div className="navbar-links">
        <button className="nav-link" onClick={onDashboardClick}>Dashboard</button>
        <button className="nav-link" onClick={onPlaceOrderClick}>Place Order</button>
        <button className="nav-link" onClick={onUserSearchClick}>User Search</button>
        <button className="nav-link" onClick={onLeaderboardClick}>Leaderboard</button>
      </div>
    </nav>
  );
}

export default Navbar;