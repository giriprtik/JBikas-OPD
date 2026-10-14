import React, { createContext, useState, useEffect } from 'react';
import axios from 'axios';
import { AUTH_API } from '../api/axiosClient.js';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('jbikas_token');
        const username = localStorage.getItem('jbikas_username');
        const roles = localStorage.getItem('jbikas_roles');

        if (token && username && roles) {
            setUser({ username, roles: roles.split(',') });
        }
        setLoading(false);
    }, []);

    const login = async (username, password) => {
        const response = await axios.post(`${AUTH_API}/login`, { username, password });
        const { token } = response.data;

        const payload = JSON.parse(atob(token.split('.')[1]));
        const roles = payload.roles; 

        localStorage.setItem('jbikas_token', token);
        localStorage.setItem('jbikas_username', username);
        localStorage.setItem('jbikas_roles', roles);

        setUser({ username, roles: roles.split(',') });
        return roles;
    };

    const logout = () => {
        localStorage.clear();
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};
