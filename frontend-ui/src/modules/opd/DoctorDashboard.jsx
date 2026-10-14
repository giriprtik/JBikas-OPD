import React, { useState, useEffect, useContext } from 'react';
import axiosClient, { OPD_API } from '../../api/axiosClient.js';
import { AuthContext } from '../../context/AuthContext';

export default function DoctorDashboard() {
    const { user, logout } = useContext(AuthContext);
    const [patients, setPatients] = useState([]);
    const [selectedId, setSelectedId] = useState('');
    const [testType, setTestType] = useState('Chest X-Ray');
    const [statusMsg, setStatusMsg] = useState('');

    const loadPatients = async () => {
        try {
            const res = await axiosClient.get(`${OPD_API}/patients/queue`);
            setPatients(res.data);
        } catch (err) { console.error(err); }
    };

    useEffect(() => { loadPatients(); }, []);

    const triggerLabOrder = async (e) => {
        e.preventDefault();
        if (!selectedId) return;
        try {
            await axiosClient.put(`${OPD_API}/patients/${selectedId}/order-lab?testType=${encodeURIComponent(testType)}`);
            setStatusMsg(`Lab referral submitted successfully for Patient ID ${selectedId}!`);
            setSelectedId('');
            loadPatients();
        } catch (err) { setStatusMsg('Failed to process cross-service lab request.'); }
    };

    return (
        <div style={{ display: 'flex', minHeight: '100vh', background: '#f4f6f9' }}>
            {/* Sidebar */}
            <div style={{ width: '260px', background: '#0f172a', color: '#fff', padding: '24px' }}>
                <h2 style={{ fontSize: '20px', margin: '0 0 40px 0', borderBottom: '1px solid #1e293b', paddingBottom: '12px' }}>🩺 JBikas OPD</h2>
                <p style={{ fontSize: '14px', color: '#94a3b8' }}>Dr. <b>{user?.username}</b></p>
                <p style={{ fontSize: '12px', background: '#1e3a8a', display: 'inline-block', padding: '4px 8px', borderRadius: '4px' }}>CLINICAL MD</p>
                <button onClick={logout} style={{ width: '100%', marginTop: '60px', padding: '10px', background: '#ef4444', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer' }}>Sign Out</button>
            </div>
            {/* Content Area */}
            <div style={{ flex: 1, padding: '40px' }}>
                <h1 style={{ fontSize: '24px', color: '#0f172a', margin: '0 0 30px 0' }}>Consultation & Examination Desk</h1>
                <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '30px' }}>
                    {/* Live Waiting Queue Cards List */}
                    <div style={{ background: '#fff', padding: '24px', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
                        <h3 style={{ margin: '0 0 20px 0' }}>Active Waiting Queue ({patients.length} Patients Remaining)</h3>
                        {patients.length === 0 ? <p style={{ color: '#64748b' }}>No patients currently waiting in queue list.</p> : (
                            <div style={{ display: 'grid', gap: '16px' }}>
                                {patients.map(p => (
                                    <div key={p.id} onClick={() => setSelectedId(p.id)} style={{ padding: '16px', borderRadius: '6px', border: selectedId === p.id ? '2px solid #2563eb' : '1px solid #e2e8f0', background: selectedId === p.id ? '#eff6ff' : '#fff', cursor: 'pointer', transition: 'all 0.2s' }}>
                                        <div style={{ display: 'flex', justifyContent: 'between', alignItems: 'center' }}>
                                            <h4 style={{ margin: '0' }}>ID: {p.id} | <b>{p.name}</b> ({p.age} Yrs / {p.gender})</h4>
                                        </div>
                                        <p style={{ margin: '8px 0 0 0', fontSize: '13px', color: '#475569' }}><b>Chief Complaint:</b> {p.chiefComplaint}</p>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                    {/* Action Block Panel */}
                    <div style={{ background: '#fff', padding: '24px', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
                        <h3 style={{ margin: '0 0 20px 0' }}>Clinical Diagnosis Tools</h3>
                        {statusMsg && <p style={{ color: 'blue', fontSize: '14px' }}>{statusMsg}</p>}
                        {selectedId ? (
                            <form onSubmit={triggerLabOrder}>
                                <p style={{ fontSize: '14px' }}>Modifying Active File for Patient ID: <b>{selectedId}</b></p>
                                <label style={{ display: 'block', fontSize: '13px', margin: '12px 0 4px' }}>Order Laboratory Investigation</label>
                                <select value={testType} onChange={e => setTestType(e.target.value)} style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #cbd5e1' }}>
                                    <option>Chest X-Ray</option><option>Complete Blood Count (CBC)</option><option>Thyroid Panel</option><option>Urine Analysis</option>
                                </select>
                                <button type="submit" style={{ width: '100%', marginTop: '20px', padding: '10px', background: '#059669', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Transmit Lab Order</button>
                            </form>
                        ) : <p style={{ color: '#64748b', fontSize: '14px' }}>Select a patient from the queue card list to activate diagnostics tools.</p>}
                    </div>
                </div>
            </div>
        </div>
    );
}
