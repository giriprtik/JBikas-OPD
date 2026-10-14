import { useContext } from 'react';
import { NavLink } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext.jsx';
import { Icon } from '../ui/index.jsx';

// Navigation per role. Add an entry here when a new module route is introduced.
const NAV_BY_ROLE = {
  ROLE_NURSE: { label: 'Nurse desk', items: [{ to: '/nurse', label: 'Front desk & triage', icon: 'clipboard' }] },
  ROLE_DOCTOR: { label: 'Doctor', items: [{ to: '/doctor', label: 'Consultation', icon: 'stethoscope' }] },
  ROLE_LAB_TECH: { label: 'Laboratory', items: [{ to: '/lab', label: 'Lab worklist', icon: 'flask' }] },
};

const initials = (name = '') => name.replace(/^(Dr|Nurse|Lab Tech)\.?\s+/i, '').split(/\s+/).map((w) => w[0]).join('').slice(0, 2);

export default function AppShell({ title, subtitle, actions, children }) {
  const { user, logout } = useContext(AuthContext);
  const sections = (user?.roles ?? []).map((role) => NAV_BY_ROLE[role]).filter(Boolean);
  const roleLabel = sections.map((s) => s.label).join(' · ') || 'Staff';

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand__mark"><Icon name="stethoscope" size={18} /></span>
          <div>
            <div className="brand__name">JBikas</div>
            <div className="brand__sub">OPD &amp; Lab Portal</div>
          </div>
        </div>

        <nav className="nav" aria-label="Main">
          {sections.map((section) => (
            <div key={section.label}>
              <div className="nav__section">{section.label}</div>
              {section.items.map((item) => (
                <NavLink key={item.to} to={item.to} className="nav__item">
                  <Icon name={item.icon} size={17} />
                  {item.label}
                </NavLink>
              ))}
            </div>
          ))}
        </nav>

        <div className="user-card">
          <span className="avatar" aria-hidden="true">{initials(user?.fullName ?? user?.username)}</span>
          <div className="user-card__meta">
            <div className="user-card__name">{user?.fullName ?? user?.username}</div>
            <div className="user-card__role">{roleLabel}</div>
          </div>
          <button type="button" className="icon-btn" onClick={logout} aria-label="Sign out" title="Sign out">
            <Icon name="logout" size={17} />
          </button>
        </div>
      </aside>

      <main className="main">
        <header className="topbar">
          <div>
            <h1 className="page-title">{title}</h1>
            {subtitle && <p className="page-subtitle">{subtitle}</p>}
          </div>
          {actions}
        </header>
        <div className="page-body">{children}</div>
      </main>
    </div>
  );
}
