// Initializes and updates the scrolling ticker bar

function initTicker(stocks) {
  const track = document.getElementById('ticker-track');
  if (!track) return;
  // Render double the items for seamless infinite loop
  const items = [...stocks, ...stocks].map(s => {
    const up = s.changePercent >= 0;
    return `
      <span class="ticker-item">
        <span class="ticker-symbol">${s.symbol}</span>
        <span class="ticker-price">${formatINR(s.currentPrice)}</span>
        <span class="ticker-change ${up ? 'up' : 'down'}">${formatPercent(s.changePercent)}</span>
      </span>`;
  }).join('');
  track.innerHTML = items;
}

function updateTicker(stocks) {
  // Update price and change values in-place without resetting animation
  stocks.forEach(s => {
    const items = document.querySelectorAll('.ticker-item');
    items.forEach(item => {
      const sym = item.querySelector('.ticker-symbol');
      if (sym && sym.textContent === s.symbol) {
        const priceEl = item.querySelector('.ticker-price');
        const changeEl = item.querySelector('.ticker-change');
        if (priceEl) priceEl.textContent = formatINR(s.currentPrice);
        if (changeEl) {
          const up = s.changePercent >= 0;
          changeEl.textContent = formatPercent(s.changePercent);
          changeEl.className = `ticker-change ${up ? 'up' : 'down'}`;
        }
      }
    });
  });
}
