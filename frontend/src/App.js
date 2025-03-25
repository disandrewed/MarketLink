// Main application component
import React, { useState } from 'react';
import './App.css';
import Navbar from './components/Navbar';
import Dashboard from './components/Dashboard';
import FocusedOrderbook from './components/FocusedOrderbook';
import UserSearch from './components/UserSearch';
import OrderForm from './components/OrderForm';
import Leaderboard from './components/Leaderboard';

function App() {
  const [view, setView] = useState('dashboard');
  const [focusedBook, setFocusedBook] = useState(null);
  
  // Function to change view to focused book
  const handleViewOrderbook = (bookName) => {
    setFocusedBook(bookName);
    setView('focused');
  };
  
  // Function to navigate back to dashboard
  const handleBackToDashboard = () => {
    setView('dashboard');
  };
  
  // Function to navigate to user search
  const handleUserSearch = () => {
    setView('userSearch');
  };
  
  // Function to navigate to place order
  const handlePlaceOrder = () => {
    setView('placeOrder');
  };
  
  // Function to navigate to leaderboard
  const handleLeaderboard = () => {
    setView('leaderboard');
  };

  return (
    <div className="app">
      <Navbar 
        onDashboardClick={handleBackToDashboard} 
        onUserSearchClick={handleUserSearch}
        onPlaceOrderClick={handlePlaceOrder}
        onLeaderboardClick={handleLeaderboard}
      />
      
      {view === 'dashboard' && (
        <Dashboard onViewOrderbook={handleViewOrderbook} />
      )}
      
      {view === 'focused' && (
        <FocusedOrderbook bookName={focusedBook} onBack={handleBackToDashboard} />
      )}
      
      {view === 'userSearch' && (
        <UserSearch onBack={handleBackToDashboard} />
      )}
      
      {view === 'placeOrder' && (
        <OrderForm onBack={handleBackToDashboard} />
      )}
      
      {view === 'leaderboard' && (
        <Leaderboard onBack={handleBackToDashboard} />
      )}
    </div>
  );
}

export default App;