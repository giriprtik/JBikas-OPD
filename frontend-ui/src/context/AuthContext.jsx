import { createContext, useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import { AUTH_API, SESSION_EXPIRED_EVENT, TOKEN_KEY } from '../api/axiosClient.js';

export const AuthContext = createContext(null);

/** Reads the claims auth-service puts in the token: sub, roles, name, exp. */
function decode(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64).split('').map((c) => `%${c.charCodeAt(0).toString(16).padStart(2, '0')}`).join(''),
    );
    const claims = JSON.parse(json);
    return {
      username: claims.sub,
      fullName: claims.name ?? claims.sub,
      roles: (claims.roles ?? '').split(',').filter(Boolean),
      expiresAt: claims.exp ? claims.exp * 1000 : null,
    };
  } catch {
    return null;
  }
}

function sessionFromStorage() {
  const token = localStorage.getItem(TOKEN_KEY);
  const session = token && decode(token);
  if (!session || (session.expiresAt && session.expiresAt <= Date.now())) {
    localStorage.removeItem(TOKEN_KEY);
    return null;
  }
  return session;
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(sessionFromStorage);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
  }, []);

  const login = async (username, password) => {
    const { data } = await axios.post(`${AUTH_API}/login`, { username, password });
    const session = decode(data.token);
    if (!session) throw new Error('Received an unreadable token');
    localStorage.setItem(TOKEN_KEY, data.token);
    setUser(session);
    return session.roles;
  };

  // Sign out when the token expires or any API call returns 401.
  useEffect(() => {
    window.addEventListener(SESSION_EXPIRED_EVENT, logout);
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, logout);
  }, [logout]);

  useEffect(() => {
    if (!user?.expiresAt) return undefined;
    const timer = setTimeout(logout, Math.max(0, user.expiresAt - Date.now()));
    return () => clearTimeout(timer);
  }, [user, logout]);

  const hasRole = (role) => Boolean(user?.roles.includes(role));

  return (
    <AuthContext.Provider value={{ user, login, logout, hasRole, loading: false }}>
      {children}
    </AuthContext.Provider>
  );
};
