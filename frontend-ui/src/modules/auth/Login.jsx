import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext.jsx';

export default function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(''); // Reset error boundary state
        try {
            const roles = await login(username, password);
            if (roles.includes('ROLE_DOCTOR')) {
                navigate('/doctor');
            } else if (roles.includes('ROLE_NURSE')) {
                navigate('/nurse');
            } else {
                setError('Account possesses unmapped system role access permissions.');
            }
        } catch (err) {
            setError('Invalid credentials or authentication microservice connection timeout.');
        }
    };

    return (
        <div style={{ 
            display: 'flex', 
            justifyContent: 'center', 
            alignItems: 'center', 
            minHeight: '100vh', 
            backgroundColor: '#f1f5f9' 
        }}>
            <div style={{ 
                width: '100%', 
                maxWidth: '400px', 
                backgroundColor: '#ffffff', 
                padding: '40px', 
                borderRadius: '8px', 
                boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06)' 
            }}>
                <h2 style={{ 
                    textAlign: 'center', 
                    color: '#0f172a', 
                    margin: '0 0 8px 0', 
                    fontSize: '24px' 
                }}>
                    JBikas Hospital System
                </h2>
                <p style={{ 
                    textAlign: 'center', 
                    color: '#64748b', 
                    margin: '0 0 32px 0', 
                    fontSize: '14px' 
                }}>
                    Sign in to access your assigned clinical portal
                </p>

                {error && (
                    <div style={{ 
                        backgroundColor: '#fef2f2', 
                        color: '#991b1b', 
                        padding: '12px', 
                        borderRadius: '6px', 
                        fontSize: '13px', 
                        marginBottom: '20px', 
                        border: '1px solid #fee2e2' 
                    }}>
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', marginBottom: '6px', fontSize: '14px', fontWeight: '500', color: '#334155' }}>
                            Username
                        </label>
                        <input 
                            type="text" 
                            placeholder="Enter your username" 
                            value={username} 
                            onChange={e => setUsername(e.target.value)} 
                            style={{ 
                                width: '100%', 
                                boxSizing: 'border-box',
                                padding: '10px 12px', 
                                borderRadius: '6px', 
                                border: '1px solid #cbd5e1', 
                                fontSize: '14px' 
                            }} 
                            required 
                        />
                    </div>

                    <div style={{ marginBottom: '24px' }}>
                        <label style={{ display: 'block', marginBottom: '6px', fontSize: '14px', fontWeight: '500', color: '#334155' }}>
                            Password
                        </label>
                        <input 
                            type="password" 
                            placeholder="Enter your password" 
                            value={password} 
                            onChange={e => setPassword(e.target.value)} 
                            style={{ 
                                width: '100%', 
                                boxSizing: 'border-box',
                                padding: '10px 12px', 
                                borderRadius: '6px', 
                                border: '1px solid #cbd5e1', 
                                fontSize: '14px' 
                             }} 
                            required 
                        />
                    </div>

                    <button 
                        type="submit" 
                        style={{ 
                            width: '100%', 
                            padding: '12px', 
                            backgroundColor: '#2563eb', 
                            color: '#ffffff', 
                            border: 'none', 
                            borderRadius: '6px', 
                            fontSize: '14px', 
                            fontWeight: '600', 
                            cursor: 'pointer',
                            transition: 'background-color 0.2s'
                        }}
                        onMouseOver={(e) => e.target.style.backgroundColor = '#1d4ed8'}
                        onMouseOut={(e) => e.target.style.backgroundColor = '#2563eb'}
                    >
                        Sign In
                    </button>
                </form>
            </div>
        </div>
    );
}
