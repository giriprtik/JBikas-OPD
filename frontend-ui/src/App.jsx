import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import Login from './modules/auth/Login.jsx';
import NurseDashboard from './modules/opd/NurseDashboard.jsx';
import DoctorDashboard from './modules/opd/DoctorDashboard.jsx';
import LabDashboard from './modules/lab/LabDashboard.jsx';

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/nurse" element={<ProtectedRoute allowedRoles={['ROLE_NURSE']}><NurseDashboard /></ProtectedRoute>} />
          <Route path="/doctor" element={<ProtectedRoute allowedRoles={['ROLE_DOCTOR']}><DoctorDashboard /></ProtectedRoute>} />
          <Route path="/lab" element={<ProtectedRoute allowedRoles={['ROLE_LAB_TECH']}><LabDashboard /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}
