/**
 * InfraWatch Theme Switcher
 * Stores preference in localStorage, applies via data-theme on <html>
 */
(function () {
  const THEMES = {
    'iot-dashboard': {
      label: 'IoT Dashboard',
      desc: 'UI UX Pro Max — Dark + Data-Dense + Real-Time Monitoring',
      badge: 'RECOMMENDED'
    },
    'mission-control': {
      label: 'Mission Control',
      desc: 'Deep slate, DM Sans + JetBrains Mono'
    },
    'cyberpunk': {
      label: 'Cyberpunk',
      desc: 'Neon accents on true black OLED'
    },
    'arctic': {
      label: 'Arctic Light',
      desc: 'Clean light theme, professional enterprise'
    }
  };

  // Apply saved theme on load (before paint)
  const saved = localStorage.getItem('iw-theme') || 'iot-dashboard';
  document.documentElement.setAttribute('data-theme', saved);

  // Build switcher UI after DOM ready
  document.addEventListener('DOMContentLoaded', function () {
    const container = document.querySelector('.user-info');
    if (!container) return;

    const wrapper = document.createElement('div');
    wrapper.className = 'theme-switcher';
    wrapper.innerHTML = `
      <button class="theme-btn" id="themeToggle" title="Switch Theme">
        <span class="theme-icon">&#9681;</span>
      </button>
      <div class="theme-dropdown" id="themeDropdown">
        <div class="theme-dropdown-header">Theme</div>
        ${Object.entries(THEMES).map(([key, t]) => `
          <button class="theme-option ${key === saved ? 'active' : ''}" data-theme="${key}">
            <span class="theme-option-dot"></span>
            <div>
              <span class="theme-option-label">${t.label}</span>
              ${t.badge ? `<span class="theme-option-badge">${t.badge}</span>` : ''}
              <span class="theme-option-desc">${t.desc}</span>
            </div>
          </button>
        `).join('')}
      </div>
    `;

    container.insertBefore(wrapper, container.firstChild);

    // Toggle dropdown
    document.getElementById('themeToggle').addEventListener('click', function (e) {
      e.stopPropagation();
      document.getElementById('themeDropdown').classList.toggle('show');
    });

    // Close on outside click
    document.addEventListener('click', function () {
      document.getElementById('themeDropdown')?.classList.remove('show');
    });

    // Theme selection
    wrapper.querySelectorAll('.theme-option').forEach(function (btn) {
      btn.addEventListener('click', function (e) {
        e.stopPropagation();
        const theme = this.getAttribute('data-theme');
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('iw-theme', theme);

        // Update active state
        wrapper.querySelectorAll('.theme-option').forEach(b => b.classList.remove('active'));
        this.classList.add('active');

        document.getElementById('themeDropdown').classList.remove('show');
      });
    });
  });
})();
