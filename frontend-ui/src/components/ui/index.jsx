// Shared presentational primitives. Styling lives in styles/global.css; these only map props to classes.
import { useEffect, useId, useRef } from 'react';
import Icon from './Icon.jsx';

const cx = (...classes) => classes.filter(Boolean).join(' ');

export { Icon };

export function Button({ variant = 'primary', size, block, loading, icon, children, className, ...rest }) {
  return (
    <button
      className={cx('btn', `btn--${variant}`, size && `btn--${size}`, block && 'btn--block', className)}
      disabled={loading || rest.disabled}
      aria-busy={loading || undefined}
      {...rest}
    >
      {loading ? <span className="spinner" aria-hidden="true" /> : icon && <Icon name={icon} size={16} />}
      {children}
    </button>
  );
}

export function Card({ title, subtitle, actions, children, bodyClassName, className, flush }) {
  return (
    <section className={cx('card', className)}>
      {(title || actions) && (
        <header className="card__header">
          <div>
            {title && <h2 className="card__title">{title}</h2>}
            {subtitle && <p className="card__subtitle">{subtitle}</p>}
          </div>
          {actions}
        </header>
      )}
      {flush ? children : <div className={cx('card__body', bodyClassName)}>{children}</div>}
    </section>
  );
}

/** Label + control wrapper. Pass the control as a render function to receive the generated id. */
export function Field({ label, hint, children }) {
  const id = useId();
  return (
    <div className="field">
      <label className="field__label" htmlFor={id}>{label}</label>
      {children(id)}
      {hint && <span className="field__hint">{hint}</span>}
    </div>
  );
}

const ALERT_ICONS = { success: 'check', error: 'alert', info: 'alert' };

export function Alert({ tone = 'info', children, onClose }) {
  return (
    <div className={`alert alert--${tone}`} role={tone === 'error' ? 'alert' : 'status'}>
      <Icon name={ALERT_ICONS[tone]} size={16} />
      <span>{children}</span>
      {onClose && (
        <button type="button" className="alert__close" onClick={onClose} aria-label="Dismiss">
          <Icon name="x" size={14} />
        </button>
      )}
    </div>
  );
}

export function Badge({ tone = 'neutral', children }) {
  return <span className={`badge badge--${tone}`}>{children}</span>;
}

export function EmptyState({ icon = 'inbox', title, children }) {
  return (
    <div className="empty">
      <span className="empty__icon"><Icon name={icon} size={20} /></span>
      <p className="empty__title">{title}</p>
      {children && <p>{children}</p>}
    </div>
  );
}

export function Skeleton({ width = '100%' }) {
  return <div className="skeleton" style={{ width }} />;
}

/** Accessible tab bar. `tabs` = [{ id, label, count? }]. */
export function Tabs({ tabs, value, onChange, label }) {
  return (
    <div className="tabs" role="tablist" aria-label={label}>
      {tabs.map((t) => (
        <button
          key={t.id}
          type="button"
          role="tab"
          aria-selected={value === t.id}
          className="tabs__tab"
          onClick={() => onChange(t.id)}
        >
          {t.label}
          {t.count != null && <span className="tabs__count">{t.count}</span>}
        </button>
      ))}
    </div>
  );
}

/**
 * Modal dialog on the native <dialog> element (focus trap and Esc for free).
 * Render it only while open: {open && <Dialog ... />}.
 */
export function Dialog({ title, children, onClose, footer }) {
  const ref = useRef(null);
  useEffect(() => {
    const el = ref.current;
    if (el && !el.open) el.showModal();
    return () => el?.open && el.close();
  }, []);
  return (
    <dialog ref={ref} className="dialog" onCancel={(e) => { e.preventDefault(); onClose(); }} aria-labelledby="dialog-title">
      <header className="dialog__header">
        <h2 id="dialog-title" className="card__title">{title}</h2>
        <button type="button" className="alert__close" onClick={onClose} aria-label="Close"><Icon name="x" size={16} /></button>
      </header>
      <div className="dialog__body">{children}</div>
      {footer && <footer className="dialog__footer">{footer}</footer>}
    </dialog>
  );
}

export function Spinner({ label = 'Loading' }) {
  return (
    <span className="page-center" role="status">
      <span className="spinner" aria-hidden="true" />
      <span className="sr-only">{label}</span>
    </span>
  );
}
