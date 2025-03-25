import React, { useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

function OrderbookDepthChart({ buyLevels, sellLevels, compact = false }) {
  // Process data for the histogram chart
  const chartData = useMemo(() => {
    if (!buyLevels || !sellLevels || buyLevels.length === 0 && sellLevels.length === 0) {
      return [];
    }

    // Extract price levels for all orders
    const allPriceLevels = [
      ...buyLevels.map(level => level[0]),
      ...sellLevels.map(level => level[0])
    ];

    // Create a map for buy and sell volumes at each price level
    const priceMap = new Map();

    // Process buy orders
    buyLevels.forEach(level => {
      const price = parseFloat(level[0].toFixed(2));
      const volume = parseFloat(level[1].toFixed(2));
      priceMap.set(price, { price, buyVolume: volume, sellVolume: 0 });
    });

    // Process sell orders
    sellLevels.forEach(level => {
      const price = parseFloat(level[0].toFixed(2));
      const volume = parseFloat(level[1].toFixed(2));
      
      if (priceMap.has(price)) {
        // Price level already exists from buy orders
        const existing = priceMap.get(price);
        priceMap.set(price, { ...existing, sellVolume: volume });
      } else {
        // New price level for sell orders
        priceMap.set(price, { price, buyVolume: 0, sellVolume: volume });
      }
    });

    // Convert map to array and sort by price
    return Array.from(priceMap.values()).sort((a, b) => a.price - b.price);
  }, [buyLevels, sellLevels]);

  // If there's no data, show a message
  if (chartData.length === 0) {
    return <div className="no-chart-data">No data available for depth chart</div>;
  }

  const maxVolume = Math.max(
    ...chartData.map(data => Math.max(data.buyVolume || 0, data.sellVolume || 0))
  );
  
  // Truncate max volume to 2 decimal places and add 10%
  const maxYDomain = parseFloat((maxVolume * 1.1).toFixed(2));

  return (
    <div className={`depth-chart ${compact ? 'compact-chart' : ''}`}>
      <ResponsiveContainer width="100%" height={compact ? 200 : 400}>
        <BarChart
          data={chartData}
          margin={compact ? {
            top: 10,
            right: 10,
            left: 10,
            bottom: 30,
          } : {
            top: 20,
            right: 30,
            left: 20,
            bottom: 50,
          }}
          barGap={0}
          barCategoryGap={1}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis 
            dataKey="price" 
            label={compact ? null : { 
              value: 'Price', 
              position: 'insideBottomRight', 
              offset: -10 
            }}
            tickFormatter={(value) => `$${value.toFixed(2)}`}
            domain={['auto', 'auto']}
            fontSize={compact ? 10 : 12}
            tick={compact ? { fontSize: 10 } : {}}
          />
          <YAxis 
            label={compact ? null : { 
              value: 'Volume', 
              angle: -90, 
              position: 'insideLeft', 
              offset: 10 
            }}
            domain={[0, maxYDomain]}
            fontSize={compact ? 10 : 12}
            tick={compact ? { fontSize: 10 } : {}}
            tickFormatter={(value) => value.toFixed(2)}
          />
          <Tooltip 
            formatter={(value, name) => {
              if (name === 'buyVolume' && value > 0) return [`${value.toFixed(2)}`, 'Buy Volume'];
              if (name === 'sellVolume' && value > 0) return [`${value.toFixed(2)}`, 'Sell Volume'];
              return ['0.00', name === 'buyVolume' ? 'Buy Volume' : 'Sell Volume'];
            }}
            labelFormatter={(label) => `Price: $${typeof label === 'number' ? label.toFixed(2) : label}`}
          />
          <Legend wrapperStyle={compact ? { fontSize: '10px' } : {}} />
          <Bar 
            dataKey="buyVolume" 
            name="Buy Orders" 
            fill="#388e3c" 
            fillOpacity={0.8}
          />
          <Bar 
            dataKey="sellVolume" 
            name="Sell Orders" 
            fill="#d32f2f" 
            fillOpacity={0.8}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

export default OrderbookDepthChart;