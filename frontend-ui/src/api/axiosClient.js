import axios from 'axios';

// Service base URLs. Override per environment with VITE_* variables (see .env.example).
export const AUTH_API = import.meta.env.VITE_AUTH_API ?? 'http://localhost:8081/api/auth';
export const OPD_API = import.meta.env.VITE_OPD_API ?? 'http://localhost:8082/api/opd';
export const LAB_API = import.meta.env.VITE_LAB_API ?? 'http://localhost:8083/api/lab';

export const TOKEN_KEY = 'jbikas_token';
export const SESSION_EXPIRED_EVENT = 'jbikas:session-expired';

const axiosClient = axios.create({
  headers: { 'Content-Type': 'application/json' },
  timeout: 20000,
});

// Attach the JWT to every request.
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// An expired or revoked token signs the user out everywhere instead of failing silently.
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
    }
    return Promise.reject(error);
  },
);

/** Shorthand: resolves to response.data. */
export const data = (promise) => promise.then((res) => res.data);

export default axiosClient;
