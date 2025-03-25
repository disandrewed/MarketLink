// Service for handling API calls
const API_BASE_URL = 'http://localhost:8080/api/exchange';

// Fetch all orderbooks
export const fetchAllOrderbooks = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/books`);
    
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error fetching all orderbooks:', error);
    throw error;
  }
};

// Fetch a single orderbook
export const fetchOrderbook = async (bookName) => {
  try {
    const response = await fetch(`${API_BASE_URL}/book/${bookName}`);
    
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error(`Error fetching orderbook ${bookName}:`, error);
    throw error;
  }
};

// Fetch user data
export const fetchUser = async (username) => {
  try {
    const response = await fetch(`${API_BASE_URL}/user/${username}`);
    
    if (!response.ok) {
      if (response.status === 400) {
        throw new Error(`User '${username}' not found`);
      }
      throw new Error(`HTTP error! Status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error(`Error fetching user ${username}:`, error);
    throw error;
  }
};

// Fetch leaderboard data
export const fetchLeaderboard = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/leaderboard`);
    
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error fetching leaderboard:', error);
    throw error;
  }
};

// Place an order
export const placeOrder = async (book, type, price, quantity, username) => {
  try {
    const url = new URL(`${API_BASE_URL}/order`);
    
    // Add query parameters
    url.search = new URLSearchParams({
      book,
      type,
      price,
      quantity,
      username
    }).toString();
    
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.error || `Failed to place order. Status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error placing order:', error);
    throw error;
  }
};