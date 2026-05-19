// Chart.js setup for candlestick and line charts on stock-detail page

let candlestickChart = null;
let lineChart = null;
let volumeChart = null;

const CHART_COLORS = {
  up:     '#00B852', // Kite vibrant green
  down:   '#FF4A4A', // Kite vibrant red
  volume: 'rgba(33,150,243,0.1)',
  text:   '#9CA3AF',
};

function initCandlestickChart(canvasId, ohlcData) {
  try {
    const ctx = document.getElementById(canvasId).getContext('2d');
    if (candlestickChart) candlestickChart.destroy();
    if (lineChart) lineChart.destroy(); // Clean up old line chart on same canvas
    
    if(!ohlcData || ohlcData.length === 0) { console.warn('No OHLC data'); return; }

    candlestickChart = new Chart(ctx, {
      type: 'candlestick',
      data: {
        datasets: [{
          label: 'OHLC',
          data: ohlcData.map(d => ({
            x: d.timestamp,
            o: d.open,
            h: d.high,
            l: d.low,
            c: d.close
          })),
          color: { up: CHART_COLORS.up, down: CHART_COLORS.down, unchanged: CHART_COLORS.text }
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false, animation: { duration: 0 },
        plugins: { legend: { display: false } },
        scales: {
          x: { type: 'time', grid: { display: false }, ticks: { maxTicksLimit: 6 } },
          y: { position: 'right', grid: { display: false },
               ticks: { callback: (v) => '₹' + v.toLocaleString() } }
        }
      }
    });
  } catch(e) { console.error('Candlestick error:', e); }
}

function initLineChart(canvasId, historyData) {
  try {
    const ctx = document.getElementById(canvasId).getContext('2d');
    if (lineChart) lineChart.destroy();
    if (candlestickChart) candlestickChart.destroy(); // Clean up old candlestick on same canvas

    if(!historyData || historyData.length === 0) { console.warn('No history data'); return; }

    const isPositive = historyData[historyData.length - 1].p >= historyData[0].p;
    const lineColor = isPositive ? CHART_COLORS.up : CHART_COLORS.down;

    lineChart = new Chart(ctx, {
      type: 'line',
      data: {
        datasets: [{
          label: 'Price',
          data: historyData.map(d => ({ x: d.t, y: d.p })),
          borderColor: lineColor, 
          borderWidth: 2, 
          fill: false, 
          tension: 0.1, 
          pointRadius: 0
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false, animation: { duration: 0 },
        plugins: { legend: { display: false } },
        scales: {
          x: { type: 'time', grid: { display: false }, ticks: { maxTicksLimit: 6 } },
          y: { position: 'right', grid: { display: false },
               ticks: { callback: (v) => '₹' + v.toLocaleString() } }
        }
      }
    });
  } catch(e) { console.error('Line chart error:', e); }
}

function initVolumeChart(canvasId, ohlcData) {
  try {
    const ctx = document.getElementById(canvasId).getContext('2d');
    if (volumeChart) volumeChart.destroy();
    
    if(!ohlcData || ohlcData.length === 0) return;

    volumeChart = new Chart(ctx, {
      type: 'bar',
      data: {
        datasets: [{
          label: 'Volume',
          data: ohlcData.map(d => ({ x: d.timestamp, y: d.volume })),
          backgroundColor: ohlcData.map(d => d.close >= d.open ? 'rgba(0,200,83,0.4)' : 'rgba(244,67,54,0.4)'),
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false, animation: { duration: 0 },
        plugins: { legend: { display: false } },
        scales: {
          x: { type: 'time', display: false },
          y: { position: 'right', ticks: { maxTicksLimit: 4, callback: (v) => v >= 1e6 ? (v/1e6).toFixed(1)+'M' : v >= 1e3 ? (v/1e3).toFixed(0)+'K' : v } }
        }
      }
    });
  } catch(e) { console.error('Volume chart error:', e); }
}

// Append a new data point to line chart from WebSocket
function appendLineDataPoint(timestamp, price) {
  if (!lineChart) return;
  const ds = lineChart.data.datasets[0];
  ds.data.push({ x: timestamp, y: price });
  if (ds.data.length > 200) ds.data.shift();  // keep last 200 points
  lineChart.update('none');  // no animation for live update
}

// Update last candlestick from WebSocket (update current open candle)
function updateLastCandle(timestamp, ohlcPoint) {
  if (!candlestickChart) return;
  const ds = candlestickChart.data.datasets[0];
  if (ds.data.length > 0) {
    ds.data[ds.data.length - 1] = ohlcPoint;
  }
  candlestickChart.update('none');
}
