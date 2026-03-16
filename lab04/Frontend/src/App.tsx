import { useState } from 'react';
import './App.css';

const GATEWAY_URL: string = (import.meta.env.GATEWAY_URL as string) ?? 'https://localhost:8079';

const GATEWAY_KEY: string = (import.meta.env.GATEWAY_KEY as string) ?? '';

// Default placeholder payloads shown in the textarea for each button
const DEFAULT_PAYLOADS: Record<number, string> = {
  1: '1', // waiterId path param for GET /api/waiters/{waiterId}
  2: JSON.stringify({ storeId: '<uuid>', standIdList: ['<uuid>', '<uuid>'] }, null, 2),
  3: JSON.stringify(
    {
      name: 'Store Name',
      brandId: '<uuid>',
      description: 'A short description of the store',
      imageUrl: 'https://example.com/image.jpg',
      timezone: 1.0,
      operatingHoursMap: {
        MON: { begin: { hour: 9, minute: 0 }, end: { hour: 22, minute: 0 } },
      },
      geoCoordinates: { longitude: -9.1393, latitude: 38.7169 },
    },
    null,
    2
  ),
};

function App() {
  const [payload, setPayload] = useState<string>(DEFAULT_PAYLOADS[1]);
  const [response, setResponse] = useState<{ code: number | null; body: string } | null>(null);
  const [loading, setLoading] = useState(false);

  /** Build and fire the request matching each gateway endpoint */
  const makeRequest = async (buttonId: number) => {
    setLoading(true);
    setResponse(null);

    try {
      let url: string;
      let method: string;
      let fetchOptions: RequestInit;

      const authHeaders: HeadersInit = GATEWAY_KEY
        ? { Authorization: GATEWAY_KEY, 'Content-Type': 'application/json' }
        : { 'Content-Type': 'application/json' };

      // GET /api/waiters/{waiterId}
      if (buttonId === 1) {
        const waiterId = payload.trim() ?? '1';
        url = `${GATEWAY_URL}/api/waiters/${encodeURIComponent(waiterId)}`;
        method = 'GET';
        fetchOptions = { method, headers: authHeaders };
      
      // POST /api/routes
      } else if (buttonId === 2) {
        url = `${GATEWAY_URL}/api/routes`;
        method = 'POST';
        fetchOptions = { method, headers: authHeaders, body: payload };
      
      // POST /api/stores
      } else {
        url = `${GATEWAY_URL}/api/stores`;
        method = 'POST';
        fetchOptions = { method, headers: authHeaders, body: payload };
      }

      const res = await fetch(url, fetchOptions);
      const data = await res.text();
      let parsedBody = data;
      try {
        parsedBody = JSON.stringify(JSON.parse(data), null, 2);
      } catch {
        // Not JSON — leave as plain text
      }

      setResponse({ code: res.status, body: parsedBody });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'An error occurred during the request.';
      setResponse({ code: null, body: message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <header className="header">
        <h1 className="title">API Explorer</h1>
        <p className="subtitle">Trigger requests to the gateway and view their responses instantly.</p>
      </header>

      <main className="main-content">
        <div className="button-group">
          <button
            className="api-btn btn-1"
            onClick={() => { setPayload(DEFAULT_PAYLOADS[1]); makeRequest(1); }}
            disabled={loading}
          >
            <span className="btn-icon">🌊</span>
            <span className="btn-label">
              <span className="btn-method">GET</span>
              <span className="btn-text">/api/waiters/&#123;id&#125;</span>
            </span>
          </button>

          <button
            className="api-btn btn-2"
            onClick={() => { setPayload(DEFAULT_PAYLOADS[2]); makeRequest(2); }}
            disabled={loading}
          >
            <span className="btn-icon">🔥</span>
            <span className="btn-label">
              <span className="btn-method">POST</span>
              <span className="btn-text">/api/routes</span>
            </span>
          </button>

          <button
            className="api-btn btn-3"
            onClick={() => { setPayload(DEFAULT_PAYLOADS[3]); makeRequest(3); }}
            disabled={loading}
          >
            <span className="btn-icon">✨</span>
            <span className="btn-label">
              <span className="btn-method">POST</span>
              <span className="btn-text">/api/stores</span>
            </span>
          </button>
        </div>

        {/* ── Payload editor ── */}
        <div className="payload-area">
          <div className="area-header">
            <h3>Request Payload</h3>
            <span className="area-hint">
              For Waiters: type the waiter ID. For Routes/Stores: paste a JSON body.
            </span>
          </div>
          <textarea
            id="payload-textbox"
            className="payload-textbox"
            value={payload}
            onChange={(e) => setPayload(e.target.value)}
            placeholder="Enter request payload or waiter ID…"
            spellCheck={false}
          />
        </div>

        {/* ── Response viewer ── */}
        <div className="response-area">
          <div className="area-header">
            <h3>Response Log</h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              {loading && <span className="loading-badge">Loading…</span>}
              {response?.code != null && (
                <span className={`status-badge ${response.code < 400 ? 'success' : 'error'}`}>
                  Status: {response.code}
                </span>
              )}
            </div>
          </div>

          <textarea
            id="response-textbox"
            className="response-textbox"
            readOnly
            value={response ? response.body : 'No response yet. Click a button above to make an API request.'}
          />
        </div>
      </main>
    </div>
  );
}

export default App;
