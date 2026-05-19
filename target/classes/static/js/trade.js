// Buy/Sell panel logic on stock-detail page

let currentMode = 'BUY';  // or 'SELL'
let currentStockPrice = 0;

function initTradePanel(stockId, currentPrice) {
  currentStockPrice = parseFloat(currentPrice);

  // Tab switching
  document.getElementById('tab-buy').addEventListener('click', () => switchTab('BUY'));
  document.getElementById('tab-sell').addEventListener('click', () => switchTab('SELL'));

  // Quantity input → live total
  document.getElementById('qty-input').addEventListener('input', updateTotal);

  // Confirm button
  document.getElementById('trade-confirm-btn').addEventListener('click', () => confirmTrade(stockId));
}

function switchTab(mode) {
  currentMode = mode;
  document.getElementById('tab-buy').classList.toggle('active', mode === 'BUY');
  document.getElementById('tab-sell').classList.toggle('active', mode === 'SELL');
  const btn = document.getElementById('trade-confirm-btn');
  if (mode === 'BUY') { btn.className = 'btn-buy'; btn.textContent = 'Buy Now'; }
  else { btn.className = 'btn-sell'; btn.textContent = 'Sell Now'; }
  updateTotal();
}

function updateTotal() {
  const qty = parseInt(document.getElementById('qty-input').value) || 0;
  const total = qty * currentStockPrice;
  document.getElementById('trade-total').textContent = formatINR(total);
}

function updateTradePrice(newPrice) {
  currentStockPrice = parseFloat(newPrice);
  updateTotal();
}

async function confirmTrade(stockId) {
  const qty = parseInt(document.getElementById('qty-input').value);
  if (!qty || qty <= 0) { showToast('Please enter a valid quantity', 'error'); return; }

  // Show confirmation modal
  const modal = new bootstrap.Modal(document.getElementById('confirmModal'));
  document.getElementById('modal-mode').textContent = currentMode;
  document.getElementById('modal-qty').textContent = qty;
  document.getElementById('modal-total').textContent = formatINR(qty * currentStockPrice);
  document.getElementById('modal-mode').className = currentMode === 'BUY' ? 'text-success' : 'text-danger';
  modal.show();

  document.getElementById('modal-confirm-btn').onclick = () => {
    modal.hide();
    executeTrade(stockId, qty);
  };
}

async function executeTrade(stockId, qty) {
  const btn = document.getElementById('trade-confirm-btn');
  setLoading(btn, true);
  const since = new Date().toISOString();

  try {
    const res = await fetch('/api/trade/' + currentMode.toLowerCase(), {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({ stockId, quantity: qty, type: currentMode })
    });
    const data = await res.json();
    if (data.success) {
      showToast(`${currentMode} order executed! ${formatINR(data.data.newBalance)} remaining`, 'success');
      document.getElementById('qty-input').value = '';
      updateTotal();
      // Update balance in sidebar
      const balEl = document.getElementById('sidebar-balance');
      if (balEl) { balEl.textContent = formatINR(data.data.newBalance); balEl.classList.add('count-anim'); }
      // Check for new badges
      setTimeout(() => checkNewBadges(since), 500);
    } else {
      showToast(data.message || 'Trade failed', 'error');
    }
  } catch (e) {
    showToast('Network error. Please try again.', 'error');
  } finally {
    setLoading(btn, false);
  }
}

async function checkNewBadges(since) {
  try {
    const res = await fetch(`/api/badges/new?since=${encodeURIComponent(since)}`, { headers: authHeaders() });
    const data = await res.json();
    if (data.success && data.data && data.data.length > 0) {
      data.data.forEach((badge, i) => {
        setTimeout(() => triggerBadgeUnlock(badge), i * 800);
      });
    }
  } catch (e) { /* silent */ }
}

function triggerBadgeUnlock(badge) {
  // Confetti burst
  confetti({ particleCount: 120, spread: 80, origin: { y: 0.6 },
    colors: ['#FFB300', '#FFF176', '#FF8F00', '#FFFFFF', '#2196F3'] });

  // Toast with badge info
  const container = document.getElementById('toast-container')
    || (() => { const d=document.createElement('div'); d.id='toast-container'; d.className='toast-container'; document.body.appendChild(d); return d; })();
  const toast = document.createElement('div');
  toast.className = 'toast success badge-unlock-anim';
  toast.style.border = '2px solid var(--gold)';
  toast.innerHTML = `
    <span style="font-size:28px">${badge.icon}</span>
    <div>
      <div style="font-weight:700;font-size:13px;color:var(--gold)">🏅 Badge Unlocked!</div>
      <div style="font-weight:600;font-size:15px">${badge.name}</div>
      <div style="font-size:12px;color:var(--text-muted)">${badge.description}</div>
    </div>`;
  container.appendChild(toast);
  setTimeout(() => { toast.classList.add('toast-exit'); setTimeout(() => toast.remove(), 300); }, 5000);
}
