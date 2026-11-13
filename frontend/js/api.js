(() => {
  const API_BASE = window.HOSTELCARE_API || 'http://localhost:8080/api';
  const token = () => sessionStorage.getItem('hc_token');
  async function request(path, options = {}) {
    const headers = new Headers(options.headers || {});
    if (token()) headers.set('Authorization', `Bearer ${token()}`);
    if (options.body && !(options.body instanceof FormData) && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
    const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
    if (response.status === 401) { sessionStorage.clear(); window.dispatchEvent(new Event('hostelcare:unauthorized')); }
    if (!response.ok) {
      let message = `Request failed (${response.status})`;
      try { const body = await response.json(); message = body.error || body.message || message; } catch (_) {}
      throw new Error(message);
    }
    if (response.status === 204) return null;
    const type = response.headers.get('content-type') || '';
    return type.includes('application/json') ? response.json() : response.text();
  }
  window.API = {
    get: p => request(p), post: (p, data) => request(p, { method: 'POST', body: data instanceof FormData ? data : JSON.stringify(data) }),
    put: (p, data) => request(p, { method: 'PUT', body: JSON.stringify(data) }),
    downloadFile: async (p, filename) => { const headers = new Headers(); if (token()) headers.set('Authorization', `Bearer ${token()}`); const r = await fetch(`${API_BASE}${p}`, { headers }); if (!r.ok) throw new Error('Could not retrieve attachment'); const blob = await r.blob(); const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = filename || 'attachment'; a.click(); URL.revokeObjectURL(url); },
    download: async p => { const headers = new Headers(); if (token()) headers.set('Authorization', `Bearer ${token()}`); const r = await fetch(`${API_BASE}${p}`, { headers }); if (!r.ok) throw new Error('Could not download report'); const blob = await r.blob(); const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = 'hostelcare-report.csv'; a.click(); URL.revokeObjectURL(url); }
  };
})();
