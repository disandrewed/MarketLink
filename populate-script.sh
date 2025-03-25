#!/bin/bash

# MarketLink Exchange Populator Script
# This script creates a realistic order book with depth and executed trades
# across multiple users and all four supported symbols
# All quantities are integers only

# Base URL for the exchange API
#BASE_URL="http://localhost:8080/api/exchange"
BASE_URL="teenage-karlotta-disandrewed-9ca204c3.koyeb.app"

# Set to 1 to show verbose output, 0 to hide
VERBOSE=1

# Function to log messages
log() {
  if [ $VERBOSE -eq 1 ]; then
    echo "[$(date +"%Y-%m-%d %H:%M:%S")] $1"
  fi
}

# Function to place an order
place_order() {
  local SYMBOL=$1
  local TYPE=$2
  local PRICE=$3
  local QUANTITY=$4  # Must be an integer
  local USERNAME=$5

  log "Placing $TYPE order: $USERNAME wants to $TYPE $QUANTITY $SYMBOL at \$$PRICE"
  
  RESPONSE=$(curl -s -X POST "$BASE_URL/order?book=$SYMBOL&type=$TYPE&price=$PRICE&quantity=$QUANTITY&username=$USERNAME")
  
  # Sleep a random amount between 50-200ms to simulate real timing
  sleep $(echo "scale=3; (50 + $RANDOM % 150) / 1000" | bc)
  
  echo $RESPONSE
}

# Define 50 user names
USERS=(
  "trader_joe" "diamond_hands" "wsb_yolo" "hodl_king" "crypto_queen"
  "buy_high_sell_low" "moon_boy" "dip_buyer" "fomo_investor" "paper_hands"
  "algo_trader" "swing_trader" "day_trader" "position_trader" "scalper"
  "value_investor" "growth_seeker" "dividend_hunter" "index_lover" "etf_builder"
  "bull_market" "bear_market" "volatility_lover" "trend_follower" "contrarian"
  "fundamental_analyst" "technical_analyst" "quant_trader" "news_trader" "earnings_player"
  "pattern_trader" "breakout_hunter" "support_resistance" "candlestick_reader" "ichimoku_master"
  "fibonacci_fan" "elliott_wave" "macd_trader" "rsi_trader" "moving_average"
  "bollinger_bands" "stochastic_user" "vwap_trader" "pivot_points" "market_maker"
  "limit_order_lover" "stop_loss_pro" "take_profit_expert" "dca_investor" "lump_sum_investor"
)

# Define realistic price ranges for each stock
AAPL_MIN=175
AAPL_MAX=195
AAPL_MID=185

AMZN_MIN=170
AMZN_MAX=190
AMZN_MID=180

NVDA_MIN=800
NVDA_MAX=900
NVDA_MID=850

MSFT_MIN=410
MSFT_MAX=430
MSFT_MID=420

# -------------------------------------------------------------------------
# PHASE 1: Build depth in the order books
# -------------------------------------------------------------------------

log "PHASE 1: Building initial order book depth..."

# Function to generate a random price within a range
random_price() {
  local MIN=$1
  local MAX=$2
  local PRECISION=${3:-2}  # Default precision is 2 decimal places
  
  echo "scale=$PRECISION; $MIN + ($MAX - $MIN) * $RANDOM / 32767" | bc
}

# Function to generate a random quantity (integers only)
random_quantity() {
  local MIN=${1:-1}    # Default min is 1
  local MAX=${2:-20}   # Default max is 20
  
  echo $(( $MIN + $RANDOM % ($MAX - $MIN + 1) ))
}

# Function to get a random user
random_user() {
  local INDEX=$((RANDOM % ${#USERS[@]}))
  echo "${USERS[$INDEX]}"
}

# Build AAPL order book - BUY orders
log "Building AAPL buy order book..."
for i in {1..40}; do
  USER=$(random_user)
  PRICE=$(random_price $((AAPL_MIN)) $((AAPL_MID-1)))
  QTY=$(random_quantity 1 20)
  place_order "AAPL" "buy" $PRICE $QTY "$USER"
done

# Build AAPL order book - SELL orders
log "Building AAPL sell order book..."
for i in {1..40}; do
  USER=$(random_user)
  PRICE=$(random_price $((AAPL_MID+1)) $((AAPL_MAX)))
  QTY=$(random_quantity 1 20)
  place_order "AAPL" "sell" $PRICE $QTY "$USER"
done

# Build AMZN order book - BUY orders
log "Building AMZN buy order book..."
for i in {1..40}; do
  USER=$(random_user)
  PRICE=$(random_price $((AMZN_MIN)) $((AMZN_MID-1)))
  QTY=$(random_quantity 1 20)
  place_order "AMZN" "buy" $PRICE $QTY "$USER"
done

# Build AMZN order book - SELL orders
log "Building AMZN sell order book..."
for i in {1..40}; do
  USER=$(random_user)
  PRICE=$(random_price $((AMZN_MID+1)) $((AMZN_MAX)))
  QTY=$(random_quantity 1 20)
  place_order "AMZN" "sell" $PRICE $QTY "$USER"
done

# Build NVDA order book - BUY orders
log "Building NVDA buy order book..."
for i in {1..40}; do
  USER=$(random_user)
  PRICE=$(random_price $((NVDA_MIN)) $((NVDA_MID-1)))
  QTY=$(random_quantity 1 20)
  place_order "NVDA" "buy" $PRICE $QTY "$USER"
done

# Build NVDA order book - SELL orders
log "Building NVDA sell order book..."
for i in {1..40}; do
  USER=$(random_user)
  PRICE=$(random_price $((NVDA_MID+1)) $((NVDA_MAX)))
  QTY=$(random_quantity 1 20)
  place_order "NVDA" "sell" $PRICE $QTY "$USER"
done

# Build MSFT order book - BUY orders
log "Building MSFT buy order book..."
for i in {1..40}; do
  USER=$(random_user)
  PRICE=$(random_price $((MSFT_MIN)) $((MSFT_MID-1)))
  QTY=$(random_quantity 1 20)
  place_order "MSFT" "buy" $PRICE $QTY "$USER"
done

# Build MSFT order book - SELL orders
log "Building MSFT sell order book..."
for i in {1..40}; do
  USER=$(random_user)
  PRICE=$(random_price $((MSFT_MID+1)) $((MSFT_MAX)))
  QTY=$(random_quantity 1 20)
  place_order "MSFT" "sell" $PRICE $QTY "$USER"
done

# -------------------------------------------------------------------------
# PHASE 2: Create trades by placing matching orders
# -------------------------------------------------------------------------

log "PHASE 2: Creating executed trades..."

# Function to create a matching trade
create_trade() {
  local SYMBOL=$1
  local PRICE=$2
  local QUANTITY=$3  # Must be an integer
  
  # Get two different random users
  local BUYER=$(random_user)
  local SELLER=$(random_user)
  
  # Make sure they're different
  while [ "$BUYER" == "$SELLER" ]; do
    SELLER=$(random_user)
  done
  
  log "Creating trade: $BUYER buys $QUANTITY $SYMBOL from $SELLER at \$$PRICE"
  
  # Place a buy order at the price
  place_order "$SYMBOL" "buy" "$PRICE" "$QUANTITY" "$BUYER"
  
  # Place a sell order at a slightly lower price to ensure a match
  SELL_PRICE=$(echo "scale=2; $PRICE - 0.01" | bc)
  place_order "$SYMBOL" "sell" "$SELL_PRICE" "$QUANTITY" "$SELLER"
  
  # Sleep between trades for a more realistic pattern
  sleep $(echo "scale=3; (100 + $RANDOM % 200) / 1000" | bc)
}

# Create AAPL trades
log "Creating AAPL trades..."
for i in {1..15}; do
  PRICE=$(random_price $((AAPL_MID-2)) $((AAPL_MID+2)))
  QTY=$(random_quantity 5 15)
  create_trade "AAPL" $PRICE $QTY
done

# Create AMZN trades
log "Creating AMZN trades..."
for i in {1..15}; do
  PRICE=$(random_price $((AMZN_MID-2)) $((AMZN_MID+2)))
  QTY=$(random_quantity 5 15)
  create_trade "AMZN" $PRICE $QTY
done

# Create NVDA trades
log "Creating NVDA trades..."
for i in {1..15}; do
  PRICE=$(random_price $((NVDA_MID-20)) $((NVDA_MID+20)))
  QTY=$(random_quantity 2 10)
  create_trade "NVDA" $PRICE $QTY
done

# Create MSFT trades
log "Creating MSFT trades..."
for i in {1..15}; do
  PRICE=$(random_price $((MSFT_MID-5)) $((MSFT_MID+5)))
  QTY=$(random_quantity 3 12)
  create_trade "MSFT" $PRICE $QTY
done

# -------------------------------------------------------------------------
# PHASE 3: Add more depth after trades to simulate continuous market
# -------------------------------------------------------------------------

log "PHASE 3: Adding more depth after trades..."

# Add more AAPL orders
log "Adding more AAPL orders..."
for i in {1..20}; do
  USER=$(random_user)
  if [ $((RANDOM % 2)) -eq 0 ]; then
    # Buy order
    PRICE=$(random_price $((AAPL_MIN)) $((AAPL_MID-1)))
    QTY=$(random_quantity 1 20)
    place_order "AAPL" "buy" $PRICE $QTY "$USER"
  else
    # Sell order
    PRICE=$(random_price $((AAPL_MID+1)) $((AAPL_MAX)))
    QTY=$(random_quantity 1 20)
    place_order "AAPL" "sell" $PRICE $QTY "$USER"
  fi
done

# Add more AMZN orders
log "Adding more AMZN orders..."
for i in {1..20}; do
  USER=$(random_user)
  if [ $((RANDOM % 2)) -eq 0 ]; then
    # Buy order
    PRICE=$(random_price $((AMZN_MIN)) $((AMZN_MID-1)))
    QTY=$(random_quantity 1 20)
    place_order "AMZN" "buy" $PRICE $QTY "$USER"
  else
    # Sell order
    PRICE=$(random_price $((AMZN_MID+1)) $((AMZN_MAX)))
    QTY=$(random_quantity 1 20)
    place_order "AMZN" "sell" $PRICE $QTY "$USER"
  fi
done

# Add more NVDA orders
log "Adding more NVDA orders..."
for i in {1..20}; do
  USER=$(random_user)
  if [ $((RANDOM % 2)) -eq 0 ]; then
    # Buy order
    PRICE=$(random_price $((NVDA_MIN)) $((NVDA_MID-1)))
    QTY=$(random_quantity 1 20)
    place_order "NVDA" "buy" $PRICE $QTY "$USER"
  else
    # Sell order
    PRICE=$(random_price $((NVDA_MID+1)) $((NVDA_MAX)))
    QTY=$(random_quantity 1 20)
    place_order "NVDA" "sell" $PRICE $QTY "$USER"
  fi
done

# Add more MSFT orders
log "Adding more MSFT orders..."
for i in {1..20}; do
  USER=$(random_user)
  if [ $((RANDOM % 2)) -eq 0 ]; then
    # Buy order
    PRICE=$(random_price $((MSFT_MIN)) $((MSFT_MID-1)))
    QTY=$(random_quantity 1 20)
    place_order "MSFT" "buy" $PRICE $QTY "$USER"
  else
    # Sell order
    PRICE=$(random_price $((MSFT_MID+1)) $((MSFT_MAX)))
    QTY=$(random_quantity 1 20)
    place_order "MSFT" "sell" $PRICE $QTY "$USER"
  fi
done

# -------------------------------------------------------------------------
# PHASE 4: Add some short positions by selling first
# -------------------------------------------------------------------------

log "PHASE 4: Creating short positions for some users..."

# Create short positions for AAPL
log "Creating AAPL short positions..."
for i in {1..5}; do
  USER=${USERS[$i]}
  PRICE=$(random_price $((AAPL_MID-2)) $((AAPL_MID)))
  QTY=$(random_quantity 10 30)
  
  # Sell first to create a short position
  log "Creating short position: $USER shorts $QTY AAPL at \$$PRICE"
  place_order "AAPL" "sell" $PRICE $QTY "$USER"
  
  # Sleep for a bit
  sleep 0.2
done

# Create short positions for AMZN
log "Creating AMZN short positions..."
for i in {6..10}; do
  USER=${USERS[$i]}
  PRICE=$(random_price $((AMZN_MID-2)) $((AMZN_MID)))
  QTY=$(random_quantity 10 30)
  
  # Sell first to create a short position
  log "Creating short position: $USER shorts $QTY AMZN at \$$PRICE"
  place_order "AMZN" "sell" $PRICE $QTY "$USER"
  
  # Sleep for a bit
  sleep 0.2
done

# Create short positions for NVDA
log "Creating NVDA short positions..."
for i in {11..15}; do
  USER=${USERS[$i]}
  PRICE=$(random_price $((NVDA_MID-20)) $((NVDA_MID)))
  QTY=$(random_quantity 5 15)
  
  # Sell first to create a short position
  log "Creating short position: $USER shorts $QTY NVDA at \$$PRICE"
  place_order "NVDA" "sell" $PRICE $QTY "$USER"
  
  # Sleep for a bit
  sleep 0.2
done

# Create short positions for MSFT
log "Creating MSFT short positions..."
for i in {16..20}; do
  USER=${USERS[$i]}
  PRICE=$(random_price $((MSFT_MID-5)) $((MSFT_MID)))
  QTY=$(random_quantity 5 20)
  
  # Sell first to create a short position
  log "Creating short position: $USER shorts $QTY MSFT at \$$PRICE"
  place_order "MSFT" "sell" $PRICE $QTY "$USER"
  
  # Sleep for a bit
  sleep 0.2
done

# -------------------------------------------------------------------------
# PHASE 5: Cover some short positions to realize profits or losses
# -------------------------------------------------------------------------

log "PHASE 5: Covering short positions to realize profits/losses..."

# Cover AAPL short positions
log "Covering AAPL short positions..."
for i in {1..5}; do
  USER=${USERS[$i]}
  
  # Sometimes cover at a profit (lower price), sometimes at a loss (higher price)
  if [ $((RANDOM % 2)) -eq 0 ]; then
    # Cover at a profit (price dropped)
    PRICE=$(random_price $((AAPL_MIN)) $((AAPL_MID-5)))
    QTY=$(random_quantity 5 10)
    log "Covering short at profit: $USER buys $QTY AAPL at \$$PRICE"
  else
    # Cover at a loss (price increased)
    PRICE=$(random_price $((AAPL_MID+2)) $((AAPL_MID+8)))
    QTY=$(random_quantity 5 10)
    log "Covering short at loss: $USER buys $QTY AAPL at \$$PRICE"
  fi
  
  # Buy to cover the short
  place_order "AAPL" "buy" $PRICE $QTY "$USER"
  
  # Sleep for a bit
  sleep 0.2
done

# Cover AMZN short positions
log "Covering AMZN short positions..."
for i in {6..10}; do
  USER=${USERS[$i]}
  
  # Sometimes cover at a profit (lower price), sometimes at a loss (higher price)
  if [ $((RANDOM % 2)) -eq 0 ]; then
    # Cover at a profit (price dropped)
    PRICE=$(random_price $((AMZN_MIN)) $((AMZN_MID-5)))
    QTY=$(random_quantity 5 10)
    log "Covering short at profit: $USER buys $QTY AMZN at \$$PRICE"
  else
    # Cover at a loss (price increased)
    PRICE=$(random_price $((AMZN_MID+2)) $((AMZN_MID+8)))
    QTY=$(random_quantity 5 10)
    log "Covering short at loss: $USER buys $QTY AMZN at \$$PRICE"
  fi
  
  # Buy to cover the short
  place_order "AMZN" "buy" $PRICE $QTY "$USER"
  
  # Sleep for a bit
  sleep 0.2
done

# Cover NVDA short positions
log "Covering NVDA short positions..."
for i in {11..15}; do
  USER=${USERS[$i]}
  
  # Sometimes cover at a profit (lower price), sometimes at a loss (higher price)
  if [ $((RANDOM % 2)) -eq 0 ]; then
    # Cover at a profit (price dropped)
    PRICE=$(random_price $((NVDA_MIN)) $((NVDA_MID-50)))
    QTY=$(random_quantity 2 8)
    log "Covering short at profit: $USER buys $QTY NVDA at \$$PRICE"
  else
    # Cover at a loss (price increased)
    PRICE=$(random_price $((NVDA_MID+20)) $((NVDA_MID+70)))
    QTY=$(random_quantity 2 8)
    log "Covering short at loss: $USER buys $QTY NVDA at \$$PRICE"
  fi
  
  # Buy to cover the short
  place_order "NVDA" "buy" $PRICE $QTY "$USER"
  
  # Sleep for a bit
  sleep 0.2
done

# Cover MSFT short positions
log "Covering MSFT short positions..."
for i in {16..20}; do
  USER=${USERS[$i]}
  
  # Sometimes cover at a profit (lower price), sometimes at a loss (higher price)
  if [ $((RANDOM % 2)) -eq 0 ]; then
    # Cover at a profit (price dropped)
    PRICE=$(random_price $((MSFT_MIN)) $((MSFT_MID-10)))
    QTY=$(random_quantity 2 8)
    log "Covering short at profit: $USER buys $QTY MSFT at \$$PRICE"
  else
    # Cover at a loss (price increased)
    PRICE=$(random_price $((MSFT_MID+5)) $((MSFT_MID+15)))
    QTY=$(random_quantity 2 8)
    log "Covering short at loss: $USER buys $QTY MSFT at \$$PRICE"
  fi
  
  # Buy to cover the short
  place_order "MSFT" "buy" $PRICE $QTY "$USER"
  
  # Sleep for a bit
  sleep 0.2
done

# -------------------------------------------------------------------------
# PHASE 6: Add some more liquid traders with many trades
# -------------------------------------------------------------------------

log "PHASE 6: Adding liquid traders with multiple trades..."

# Create some super active traders
ACTIVE_TRADERS=("algo_trader" "day_trader" "market_maker" "swing_trader" "scalper")

# For each active trader
for TRADER in "${ACTIVE_TRADERS[@]}"; do
  log "Creating trading activity for $TRADER..."
  
  # Each active trader makes multiple trades across all symbols
  for SYMBOL in "AAPL" "AMZN" "NVDA" "MSFT"; do
    # Determine appropriate price ranges for the symbol
    case $SYMBOL in
      "AAPL")
        MIN=$AAPL_MIN
        MAX=$AAPL_MAX
        MID=$AAPL_MID
        ;;
      "AMZN")
        MIN=$AMZN_MIN
        MAX=$AMZN_MAX
        MID=$AMZN_MID
        ;;
      "NVDA")
        MIN=$NVDA_MIN
        MAX=$NVDA_MAX
        MID=$NVDA_MID
        ;;
      "MSFT")
        MIN=$MSFT_MIN
        MAX=$MSFT_MAX
        MID=$MSFT_MID
        ;;
    esac
    
    # Make 5-10 trades per symbol
    NUM_TRADES=$((5 + RANDOM % 6))
    
    for ((i=1; i<=NUM_TRADES; i++)); do
      # Randomly decide to buy or sell
      if [ $((RANDOM % 2)) -eq 0 ]; then
        # Buy
        PRICE=$(random_price $((MIN)) $((MID)))
        QTY=$(random_quantity 5 25)
        log "$TRADER buys $QTY $SYMBOL at \$$PRICE"
        place_order "$SYMBOL" "buy" $PRICE $QTY "$TRADER"
      else
        # Sell
        PRICE=$(random_price $((MID)) $((MAX)))
        QTY=$(random_quantity 5 25)
        log "$TRADER sells $QTY $SYMBOL at \$$PRICE"
        place_order "$SYMBOL" "sell" $PRICE $QTY "$TRADER"
      fi
      
      # Sleep a bit between orders
      sleep 0.1
    done
  done
done

log "Order book population complete!"
log "Summary:"
log "- 50 unique traders"
log "- 4 symbols: AAPL, AMZN, NVDA, MSFT"
log "- Multiple buy orders, sell orders, and executed trades"
log "- Short positions and covers included"
log "- Extra liquidity from active traders"
log "- All quantities are integers"

# Done!