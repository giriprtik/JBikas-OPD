import React, { useState, useEffect, useContext } from 'react';
import axiosClient, { OPD_API } from '../../api/axiosClient.js';
import { AuthContext } from '../../context/AuthContext';

export default function NurseDashboard() {
    const { user, logout } = useContext(AuthContext);
    const [queue, setQueue] = useState([]);
    const [formData, setFormData] = useState({ name: '', age: '', gender: 'Male', contactNumber: '', chiefComplaint: '' });
    const [msg, setMsg] = useState('');

    const fetchQueue = async () => {
        try {
            const res = await axiosClient.get(`${OPD_API}/patients/all`);
            setQueue(res.data);
        } catch (err) { console.error(err); }
    };

    useEffect(() => { fetchQueue(); }, []);

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            await axiosClient.post(`${OPD_API}/patients/register`, formData);
            setMsg('Patient registered to queue successfully!');
            setFormData({ name: '', age: '', gender: 'Male', contactNumber: '', chiefComplaint: '' });
            fetchQueue();
        } catch (err) { setMsg('Registration failure.'); }
    };

    return (
        <div style={{ display: 'flex', minHeight: '100vh', background: '#f4f6f9' }}>
            {/* Sidebar Navigator */}
            <div style={{ width: '260px', background: '#1e293b', color: '#fff', padding: '24px' }}>
                <h2 style={{ fontSize: '20px', margin: '0 0 40px 0', borderBottom: '1px solid #334155', paddingBottom: '12px' }}>🏥 JBikas OPD</h2>
                <p style={{ fontSize: '14px', color: '#94a3b8' }}>User: <b>{user?.username}</b></p>
                <p style={{ fontSize: '12px', background: '#334155', display: 'inline-block', padding: '4px 8px', borderRadius: '4px' }}>NURSE DESK</p>
                <button onClick={logout} style={{ width: '100%', marginTop: '60px', padding: '10px', background: '#ef4444', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer' }}>Sign Out</button>
            </div>
            {/* Main Content Area */}
            <div style={{ flex: 1, padding: '40px' }}>
                <h1 style={{ fontSize: '24px', color: '#0f172a', margin: '0 0 30px 0' }}>OPD Intake Management Portal</h1>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '30px' }}>
                    {/* Form Block */}
                    <div style={{ background: '#fff', padding: '24px', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
                        <h3 style={{ margin: '0 0 20px 0' }}>Register New Patient</h3>
                        {msg && <p style={{ color: msg.includes('success') ? 'green' : 'red' }}>{msg}</p>}
                        <form onSubmit={handleRegister}>
                            <label style={{ display: 'block', fontSize: '13px', margin: '8px 0 4px' }}>Full Name</label>
                            <input type="text" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} style={{ width: '90%', padding: '8px', borderRadius: '4px', border: '1px solid #cbd5e1' }} required />
                            <label style={{ display: 'block', fontSize: '13px', margin: '8px 0 4px' }}>Age</label>
                            <input type="number" value={formData.age} onChange={e => setFormData({...formData, age: e.target.value})} style={{ width: '90%', padding: '8px', borderRadius: '4px', border: '1px solid #cbd5e1' }} required />
                            <label style={{ display: 'block', fontSize: '13px', margin: '8px 0 4px' }}>Gender</label>
                            <select value={formData.gender} onChange={e => setFormData({...formData, gender: e.target.value})} style={{ width: '96%', padding: '8px', borderRadius: '4px', border: '1px solid #cbd5e1' }}>
                                <option>Male</option><option>Female</option><option>Other</option>
                            </select>
                            <label style={{ display: 'block', fontSize: '13px', margin: '8px 0 4px' }}>Contact Number</label>
                            <input type="text" value={formData.contactNumber} onChange={e => setFormData({...formData, contactNumber: e.target.value})} style={{ width: '90%', padding: '8px', borderRadius: '4px', border: '1px solid #cbd5e1' }} required />
                            <label style={{ display: 'block', fontSize: '13px', margin: '8px 0 4px' }}>Chief Complaint</label>
                            <textarea value={formData.chiefComplaint} onChange={e => setFormData({...formData, chiefComplaint: e.target.value})} style={{ width: '90%', padding: '8px', borderRadius: '4px', border: '1px solid #cbd5e1', height: '60px' }} required />
                            <button type="submit" style={{ width: '97%', marginTop: '16px', padding: '10px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Add to Waitlist Queue</button>
                        </form>
                    </div>
                    {/* Live Records Log Table View */}
                    <div style={{ background: '#fff', padding: '24px', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
                        <h3 style={{ margin: '0 0 20px 0' }}>Patient Records Master Log</h3>
                        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                            <thead>
                                <tr style={{ borderBottom: '2px solid #e2e8f0', color: '#64748b', fontSize: '14px' }}>
                                    <th style={{ padding: '12px' }}>ID</th><th style={{ padding: '12px' }}>Name</th><th style={{ padding: '12px' }}>Age/Sex</th><th style={{ padding: '12px' }}>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {queue.map(p => (
                                    <tr key={p.id} style={{ borderBottom: '1px solid #e2e8f0', fontSize: '14px' }}>
                                        <td style={{ padding: '12px' }}>{p.id}</td><td style={{ padding: '12px' }}><b>{p.name}</b></td><td style={{ padding: '12px' }}>{p.age} Yrs / {p.gender}</td>
                                        <td style={{ padding: '12px' }}><span style={{ padding: '4px 8px', borderRadius: '12px', fontSize: '12px', fontWeight: 'bold', background: p.status === 'QUEUED' ? '#dbeafe' : '#fef3c7', color: p.status === 'QUEUED' ? '#1e40af' : '#92400e' }}>{p.status}</span></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}
