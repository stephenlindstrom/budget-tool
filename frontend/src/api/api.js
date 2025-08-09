import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
});

let getToken = null;
let onUnauthorized = null;

/**
 * Configure how the API gets tokens and handles 401s.
 * Call this once at app start (or when token changes).
 */

export function configureApi({ getToken: _getToken, onUnauthorized: _onUnauthorized } = {}) {
  getToken = _getToken || null;
  onUnauthorized = _onUnauthorized || null;
}

// Request interceptor: attach Authorization if we have a token
api.interceptors.request.use(
  (config) => {
    // preserve existing headers & the abort signal if present
    const headers = config.headers ?? {};

    if (typeof getToken === "function") {
      const token = getToken();
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      } else {
        delete headers.Authorization;
      }
    }

    return { ...config, headers };
  },
  (error) => Promise.reject(error)
);

// Response interceptor: handle 401s centrally
api.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error?.response?.status;
    if (status === 401 && typeof onUnauthorized === "function") {
      try { onUnauthorized(error); } catch { /* noop */ }
    }
    return Promise.reject(error);
  }
);

export default api;