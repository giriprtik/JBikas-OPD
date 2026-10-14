import { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext.jsx';
import { Button, Card, EmptyState } from './ui/index.jsx';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, loading, logout } = useContext(AuthContext);

  if (loading) {
    return (
      <div className="page-center" role="status">
        <span className="spinner" aria-hidden="true" />
        <span className="sr-only">Loading session…</span>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.some((role) => user.roles.includes(role))) {
    return (
      <div className="page-center">
        <Card>
          <EmptyState icon="lock" title="You don't have access to this page">
            Signed in as {user.username}. Sign in with an account that has the right role.
          </EmptyState>
          <Button variant="secondary" block onClick={logout}>Sign in as someone else</Button>
        </Card>
      </div>
    );
  }

  return children;
};

export default ProtectedRoute;
