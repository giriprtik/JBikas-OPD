import { useMemo, useState } from 'react';
import { Alert, Button, Dialog, Field } from '../../components/ui/index.jsx';
import { FlagMark } from '../../components/clinical/index.jsx';
import { FLAG, isAbnormal, isCritical, previewResultFlag } from '../../domain/clinical.js';
import { labApi } from '../../api/labApi.js';
import useAction from '../../hooks/useAction.js';

/**
 * Result entry for one order. Flags preview while typing (same rules as the server); the saved flag
 * always comes from lab-service. "Release" saves pending edits first.
 */
export default function ResultEntryForm({ order, onChange }) {
  const initial = useMemo(
    () => Object.fromEntries(order.items.flatMap((i) => i.results).map((r) => [r.id, r.value ?? ''])),
    [order],
  );
  const [values, setValues] = useState(initial);
  const [releasing, setReleasing] = useState(false);
  const [saved, setSaved] = useState(false);
  const save = useAction();

  const results = order.items.flatMap((i) => i.results);
  const changed = results.filter((r) => (values[r.id] ?? '') !== (initial[r.id] ?? ''));
  const previews = Object.fromEntries(results.map((r) => [r.id, previewResultFlag(r, values[r.id])]));
  const invalid = results.filter((r) => previews[r.id] === 'INVALID');
  const missing = results.filter((r) => r.valueType !== 'NARRATIVE' && !String(values[r.id] ?? '').trim());
  const critical = results.filter((r) => isCritical(previews[r.id]));

  /** Saves edited values. Resolves to the order (unchanged or updated), or undefined if the save failed. */
  const persist = async () => {
    if (!changed.length) return order;
    const updated = await save.run(
      () => labApi.saveResults(order.id, changed.map((r) => ({ resultId: r.id, value: values[r.id] }))),
      'Could not save results.',
    );
    if (updated) onChange(updated);
    return updated;
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (await persist()) setSaved(true);
  };

  return (
    <>
      <form className="form-grid" onSubmit={handleSave}>
        {save.error && <Alert tone="error" onClose={() => save.setError('')}>{save.error}</Alert>}
        {saved && !changed.length && <Alert tone="success" onClose={() => setSaved(false)}>Draft saved. Release when all results are checked.</Alert>}

        {order.items.map((item) => (
          <div key={item.id} className="results__group">
            <div className="results__test">
              {item.testName}
              {item.specimen && <span className="muted"> · {item.specimen}</span>}
            </div>
            <div className="entry">
              {item.results.map((r) => {
                const flag = previews[r.id];
                const tone = flag === 'INVALID' || isCritical(flag) ? 'entry__row--critical' : isAbnormal(flag) ? 'entry__row--abnormal' : '';
                const inputId = `result-${r.id}`;
                return (
                  <div key={r.id} className={`entry__row ${r.valueType === 'NARRATIVE' ? 'entry__row--narrative' : ''} ${tone}`}>
                    <label className="entry__name" htmlFor={inputId}>{r.parameterName}</label>
                    <div className="entry__input">
                      {r.valueType === 'NARRATIVE' ? (
                        <textarea id={inputId} className="input" value={values[r.id] ?? ''}
                          onChange={(e) => setValues((s) => ({ ...s, [r.id]: e.target.value }))} />
                      ) : (
                        <input
                          id={inputId}
                          className="input"
                          inputMode={r.valueType === 'NUMERIC' ? 'decimal' : 'text'}
                          list={r.valueType === 'TEXT' && r.normalValues.length ? `normals-${r.id}` : undefined}
                          autoComplete="off"
                          value={values[r.id] ?? ''}
                          aria-describedby={`ref-${r.id}`}
                          onChange={(e) => setValues((s) => ({ ...s, [r.id]: e.target.value }))}
                        />
                      )}
                      {r.valueType === 'TEXT' && r.normalValues.length > 0 && (
                        <datalist id={`normals-${r.id}`}>{r.normalValues.map((n) => <option key={n} value={n} />)}</datalist>
                      )}
                      {r.unit && <span className="entry__unit">{r.unit}</span>}
                    </div>
                    <div className="entry__ref" id={`ref-${r.id}`}>
                      {flag === 'INVALID'
                        ? <span className="value--abnormal">Not a number</span>
                        : <>{r.referenceText && <span className="cell-secondary">{r.referenceText}</span>} <FlagMark flag={flag} /></>}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))}

        {critical.length > 0 && (
          <Alert tone="error">
            Critical: {critical.map((r) => `${r.parameterName} ${values[r.id]}`).join(', ')}. Repeat or verify, then phone the
            doctor before releasing.
          </Alert>
        )}

        <div className="button-row">
          <Button type="submit" variant="secondary" loading={save.busy} disabled={!changed.length || invalid.length > 0}>
            Save draft
          </Button>
          <Button
            type="button"
            icon="check"
            disabled={missing.length > 0 || invalid.length > 0 || save.busy}
            title={missing.length ? `Missing: ${missing.map((r) => r.parameterName).join(', ')}` : undefined}
            onClick={() => setReleasing(true)}
          >
            Release report
          </Button>
        </div>
        {missing.length > 0 && <p className="field__hint">{missing.length} result{missing.length > 1 ? 's' : ''} still to enter before release.</p>}
      </form>

      {/* Rendered outside the form above: a <form> inside a <form> is invalid HTML and its submit event
          would also bubble to handleSave. */}
      {releasing && (
        <ReleaseDialog
          order={order}
          critical={critical}
          values={values}
          onClose={() => setReleasing(false)}
          beforeRelease={persist}
          onReleased={(updated) => { setReleasing(false); onChange(updated); }}
        />
      )}
    </>
  );
}

function ReleaseDialog({ order, critical, values, onClose, beforeRelease, onReleased }) {
  const [notified, setNotified] = useState('');
  const release = useAction();

  const submit = async (e) => {
    e.preventDefault();
    console.log('[lab] release submitted for order', order.id); // temporary: remove once release works

    // Save any pending edits first; stop if that fails so we never release stale values.
    const savedOrder = await beforeRelease();
    if (!savedOrder) {
      release.setError('Could not save the latest values. Fix the problem shown on the form and try again.');
      return;
    }

    const updated = await release.run(
      () => labApi.release(order.id, notified.trim()),
      'Could not release the report.',
    );
    if (updated) onReleased(updated);
  };

  return (
    <Dialog title={`Release ${order.orderNumber}?`} onClose={onClose}>
      <form className="form-grid" onSubmit={submit}>
        <p>
          The doctor will see these results immediately and the report becomes final. Later corrections are recorded as
          amendments.
        </p>
        {release.error && <Alert tone="error">{release.error}</Alert>}
        {critical.length > 0 && (
          <>
            <Alert tone="error">
              {critical.map((r) => `${r.parameterName} ${values[r.id]} (${FLAG[previewResultFlag(r, values[r.id])]?.label})`).join(', ')}
            </Alert>
            <Field label="Critical values phoned to" hint="Name of the doctor or nurse informed, and the time.">
              {(id) => (
                <input id={id} className="input" required autoFocus placeholder={`e.g. ${order.orderingDoctorName ?? 'Dr. …'}, 14:20`}
                  value={notified} onChange={(e) => setNotified(e.target.value)} />
              )}
            </Field>
          </>
        )}
        <div className="button-row">
          <Button type="button" variant="secondary" onClick={onClose}>Back</Button>
          <Button type="submit" icon="check" loading={release.busy}>Release</Button>
        </div>
      </form>
    </Dialog>
  );
}
