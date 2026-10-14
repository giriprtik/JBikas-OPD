import { useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext.jsx';
import { Alert, Button, Field, Icon } from '../../components/ui/index.jsx';

// Landing route for each role after sign-in. First match wins.
export const ROLE_HOME = [
  ['ROLE_DOCTOR', '/doctor'],
  ['ROLE_NURSE', '/nurse'],
  ['ROLE_LAB_TECH', '/lab'],
];

const homeFor = (roles = []) => ROLE_HOME.find(([role]) => roles.includes(role))?.[1];

export default function Login() {
  const { user, login } = useContext(AuthContext);
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  // Already signed in: skip the form.
  useEffect(() => {
    const home = homeFor(user?.roles);
    if (home) navigate(home, { replace: true });
  }, [user, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const roles = await login(username.trim(), password);
      const home = homeFor(roles);
      if (home) navigate(home, { replace: true });
      else if (roles.includes('ROLE_ADMIN')) setError('Administrator accounts manage users through the auth-service API; there is no admin screen yet.');
      else setError('Your account has no portal assigned. Contact the system administrator.');
    } catch (err) {
      setError(
        err?.response
          ? 'Incorrect username or password.'
          : 'Cannot reach the authentication service. Try again in a moment.',
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth">
      <aside className="auth__aside">
        <div className="brand">
          <span className="brand__mark"><Icon name="stethoscope" size={18} /></span>
          <div>
            <div className="brand__name">JBikas Hospital</div>
            <div className="brand__sub">OPD &amp; Lab Portal</div>
          </div>
        </div>
        <div>
          <p className="auth__headline">Outpatient care, from registration to lab results.</p>
          <ul className="auth__points">
            <li><Icon name="clipboard" /> Register patients and manage the waiting queue</li>
            <li><Icon name="stethoscope" /> Review complaints and consult in order</li>
            <li><Icon name="flask" /> Lab results flagged, released and reported as PDF</li>
          </ul>
        </div>
        <p className="field__hint">Authorised hospital staff only.</p>
      </aside>

      <main className="auth__main">
        <div className="auth__panel">
          <div className="brand auth__mobile-brand">
            <span className="brand__mark"><Icon name="stethoscope" size={18} /></span>
            <div>
              <div className="brand__name">JBikas Hospital</div>
              <div className="brand__sub">OPD &amp; Lab Portal</div>
            </div>
          </div>
          <div>
            <h1 className="auth__title">Sign in</h1>
            <p className="page-subtitle">Use your hospital staff credentials.</p>
          </div>

          {error && <Alert tone="error">{error}</Alert>}

          <form className="form-grid" onSubmit={handleSubmit}>
            <Field label="Username">
              {(id) => (
                <input
                  id={id}
                  className="input"
                  autoComplete="username"
                  autoFocus
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />
              )}
            </Field>

            <Field label="Password">
              {(id) => (
                <div className="password-wrap">
                  <input
                    id={id}
                    className="input"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowPassword((v) => !v)}
                    aria-pressed={showPassword}
                  >
                    {showPassword ? 'Hide' : 'Show'}
                  </button>
                </div>
              )}
            </Field>

            <Button type="submit" block loading={submitting} icon="lock">
              {submitting ? 'Signing in…' : 'Sign in'}
            </Button>
          </form>
        </div>
      </main>
    </div>
  );
}
