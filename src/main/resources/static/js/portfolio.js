// Portfolio page — loads summary, holdings table, portfolio line chart

async function loadPortfolio() {
  requireAuth();
  try {
    const res = await fetch('/api/portfolio', { headers: authHeaders() });
    const data = await res.json();
    if (!data.success) return;
    const p = data.data;

    // Summary cards
    document.getElementById('cash-balance').textContent = formatINR(p.cashBalance);
    document.getElementById('total-invested').textContent = formatINR(p.totalInvested);
    document.getElementById('current-value').textContent = formatINR(p.currentValue);
    const pnlEl = document.getElementById('total-pnl');
    pnlEl.textContent = formatINR(p.totalPnl) + ' (' + formatPercent(p.totalPnlPercent) + ')';
    pnlEl.className = 'stat-value ' + (p.isProfit ? 'price-up' : 'price-down');

    // Holdings table
    const tbody = document.getElementById('holdings-tbody');
    if (p.holdings.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:40px;color:var(--text-muted)">
        No holdings yet. <a href="/dashboard">Start trading →</a></td></tr>`;
    } else {
      tbody.innerHTML = p.holdings.map(h => `
        <tr>
          <td><a href="/stocks/${h.stockId}" style="text-decoration:none">
            <strong>${h.symbol}</strong>
            <div style="font-size:12px;color:var(--text-muted)">${h.companyName}</div>
          </a></td>
          <td class="mono">${h.quantity}</td>
          <td class="mono">${formatINR(h.avgBuyPrice)}</td>
          <td class="mono">${formatINR(h.currentPrice)}</td>
          <td class="mono">${formatINR(h.currentValue)}</td>
          <td class="mono ${h.isProfit ? 'price-up' : 'price-down'}">
            ${formatINR(h.unrealizedPnl)}<br>
            <span style="font-size:12px">${formatPercent(h.unrealizedPnlPercent)}</span>
          </td>
          <td>
            <a href="/stocks/${h.stockId}" class="btn-outline" style="padding:6px 12px;font-size:12px">Trade</a>
          </td>
        </tr>`).join('');
    }

    // Portfolio line chart
    loadPortfolioChart();
  } catch(e) { showToast('Failed to load portfolio', 'error'); }
}

let portfolioChartInstance = null;

async function loadPortfolioChart() {
  try {
    const res = await fetch('/api/portfolio/history', { headers: authHeaders() });
    const data = await res.json();
    
    if(!data.data || data.data.length === 0) return;

    if (portfolioChartInstance) {
        portfolioChartInstance.destroy();
    }

    const currentVal = data.data[data.data.length - 1].v;
    const isProfit = currentVal >= 100000;
    const colorHex = isProfit ? '#00C853' : '#F44336';
    const colorRgba = isProfit ? 'rgba(0,200,83,' : 'rgba(244,67,54,';

    const ctx = document.getElementById('portfolio-chart').getContext('2d');
    const gradient = ctx.createLinearGradient(0, 0, 0, 200);
    gradient.addColorStop(0, colorRgba + '0.3)');
    gradient.addColorStop(1, colorRgba + '0)');

    portfolioChartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        datasets: [{
          data: data.data.map(d => ({ x: d.t, y: d.v })),
          borderColor: colorHex, backgroundColor: gradient,
          borderWidth: 2, fill: true, tension: 0.1, pointRadius: 0
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        animation: { duration: 0 },
        plugins: { legend: { display: false } },
        scales: {
          x: { type: 'time', grid: { display: false },
            ticks: { color: '#9CA3AF', maxTicksLimit: 8 } },
          y: { position: 'right', grid: { color: 'rgba(0,0,0,0.05)' },
            ticks: { color: '#9CA3AF', callback: v => '₹' + (v/1000).toFixed(0)+'K' } }
        }
      }
    });
  } catch(e) { console.error("Portfolio chart error", e); }
}
