import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';

// Core Feature Module Imports with explicit file extensions
import Login from './modules/auth/Login.jsx';
import NurseDashboard from './modules/opd/NurseDashboard.jsx';
import DoctorDashboard from './modules/opd/DoctorDashboard.jsx';

function App() {
    return (
        <AuthProvider>
            <Router>
                <div style={{ fontFamily: 'sans-serif', margin: 0, padding: 0 }}>
                    <Routes>
                        {/* Public Gateway Route */}
                        <Route path="/login" element={<Login />} />
                        
                        {/* 📝 NURSE PORTAL: Guarded by Nurse clearances */}
                        <Route path="/nurse" element={
                            <ProtectedRoute allowedRoles={['ROLE_NURSE']}>
                                <NurseDashboard />
                            </ProtectedRoute>
                        } />

                        {/* 🩺 DOCTOR PORTAL: Guarded by Doctor clearances */}
                        <Route path="/doctor" element={
                            <ProtectedRoute allowedRoles={['ROLE_DOCTOR']}>
                                <DoctorDashboard />
                            </ProtectedRoute>
                        } />

                        {/* Route Fallback Catch-all */}
                        <Route path="*" element={<Navigate to="/login" replace />} />
                    </Routes>
                </div>
            </Router>
        </AuthProvider>
    );
}

export default App;
