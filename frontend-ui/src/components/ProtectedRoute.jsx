import React, { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext.jsx';

const ProtectedRoute = ({ children, allowedRoles }) => {
    const { user, loading } = useContext(AuthContext);

    if (loading) {
        return <div style={{ padding: '20px' }}>Loading clearances context...</div>;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles && !allowedRoles.some(role => user.roles.includes(role))) {
        return (
            <div style={{ padding: '40px', textAlign: 'center', color: '#991b1b' }}>
                <h2>403 - Access Denied</h2>
                <p>Your user group profile does not possess authorization metrics for this dashboard module.</p>
            </div>
        );
    }

    return children;
};

export default ProtectedRoute;
