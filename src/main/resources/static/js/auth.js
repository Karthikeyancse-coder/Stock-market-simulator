// Shared auth utility functions used by login.html and register.html

const BASE = '';

function getToken() { return localStorage.getItem('token'); }
function setToken(t) { localStorage.setItem('token', t); }
function setUser(u)  { localStorage.setItem('user', JSON.stringify(u)); }
function getUser()   { return JSON.parse(localStorage.getItem('user') || '{}'); }
function clearAuth() { localStorage.removeItem('token'); localStorage.removeItem('user'); }

function authHeaders() {
  return { 'Content-Type': 'application/json', 'Authorization': `Bearer ${getToken()}` };
}

// Redirect to dashboard if already logged in
function redirectIfAuth() {
  if (getToken()) window.location.href = '/dashboard';
}

// Guard: redirect to login if no token
function requireAuth() {
  if (!getToken()) window.location.href = '/auth/login';
}

// Toast system
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container')
    || (() => { const d = document.createElement('div'); d.id='toast-container'; d.className='toast-container'; document.body.appendChild(d); return d; })();
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  const icons = { success: '✅', error: '❌', info: 'ℹ️' };
  toast.innerHTML = `<span class="toast-icon">${icons[type]||'ℹ️'}</span><span class="toast-msg">${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => { toast.classList.add('toast-exit'); setTimeout(() => toast.remove(), 300); }, 3500);
}

// Format currency
function formatINR(amount) {
  return '₹' + Number(amount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function formatPercent(val) {
  const sign = val >= 0 ? '+' : '';
  return `${sign}${Number(val).toFixed(2)}%`;
}

// Set button loading state
function setLoading(btn, loading) {
  if (loading) {
    btn.dataset.orig = btn.innerHTML;
    btn.innerHTML = '<span class="spinner"></span> Loading...';
    btn.disabled = true;
  } else {
    btn.innerHTML = btn.dataset.orig;
    btn.disabled = false;
  }
}
