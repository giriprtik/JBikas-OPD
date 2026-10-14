import axios from 'axios';

// Base configurations mapping to our specific port endpoints
export const AUTH_API = 'http://localhost:8081/api/auth';
export const OPD_API = 'http://localhost:8082/api/opd';
export const LAB_API = 'http://localhost:8083/api/lab';

const axiosClient = axios.create({
    headers: {
        'Content-Type': 'application/json',
    },
});

// Auto-inject the JWT token into all outgoing requests
axiosClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('jbikas_token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

export default axiosClient;
