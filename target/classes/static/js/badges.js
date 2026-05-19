// Badges page — renders badge grid and handles unlock animations

async function loadBadges() {
  const grid = document.getElementById('badges-grid');
  grid.innerHTML = Array(10).fill('<div class="skeleton" style="height:200px;border-radius:12px"></div>').join('');

  try {
    const res = await fetch('/api/badges', { headers: authHeaders() });
    const data = await res.json();
    if (!data.success) return;

    grid.innerHTML = data.data.map(badge => renderBadgeCard(badge)).join('');

    // Stats
    const earned = data.data.filter(b => b.earned).length;
    document.getElementById('badge-count').textContent = `${earned} / ${data.data.length} Earned`;
  } catch (e) {
    grid.innerHTML = '<p class="text-muted">Failed to load badges.</p>';
  }
}

function renderBadgeCard(badge) {
  const progressWidth = Math.min(100, badge.progressPercent || 0);
  const earnedDate = badge.earnedAt
    ? new Date(badge.earnedAt).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric' })
    : null;

  return `
    <div class="badge-card ${badge.earned ? 'earned anim-card' : 'locked anim-card'}">
      <span class="badge-emoji" style="${badge.earned ? '' : 'filter:grayscale(0.8)'}">${badge.icon}</span>
      <div class="badge-name">${badge.name}</div>
      <div class="badge-desc">${badge.description}</div>
      ${!badge.earned && badge.progressTarget ? `
        <div class="badge-progress" title="${badge.currentProgress || 0} / ${badge.progressTarget}">
          <div class="badge-progress-bar" style="width:${progressWidth}%"></div>
        </div>
        <div style="font-size:11px;color:var(--text-muted);margin-top:4px">
          ${badge.currentProgress || 0} / ${badge.progressTarget}
        </div>` : ''}
      ${badge.earned && earnedDate ? `<div class="badge-earned-date">✓ Earned ${earnedDate}</div>` : ''}
      ${badge.earned ? '<div style="position:absolute;top:12px;right:12px;color:var(--gold);font-size:16px">⭐</div>' : ''}
    </div>`;
}
