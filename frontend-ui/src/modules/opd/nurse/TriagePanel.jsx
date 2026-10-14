import { useEffect, useState } from 'react';
import { Alert, Button, Card, EmptyState, Field } from '../../../components/ui/index.jsx';
import { FlagMark, PatientBanner, StatusBadge, VitalsSummary } from '../../../components/clinical/index.jsx';
import { PRIORITY, VISIT_STATUS, VITALS, ageSex, elapsedSince, isCritical, previewVitalFlag } from '../../../domain/clinical.js';
import { visitApi } from '../../../api/opdApi.js';
import useAction from '../../../hooks/useAction.js';
import useAsync from '../../../hooks/useAsync.js';
import useRevealOnNarrow from '../../../hooks/useRevealOnNarrow.js';

const TRIAGE_STATUSES = ['REGISTERED', 'TRIAGED', 'IN_CONSULTATION', 'AWAITING_LAB', 'LAB_RESULTS_READY'];

/** Triage: patients waiting for vitals first, then everyone still in the OPD (vitals can be repeated). */
export default function TriagePanel({ visits, selectedId, onSelect, onSaved }) {
  const waiting = visits.filter((v) => v.status === 'REGISTERED');
  const others = visits.filter((v) => TRIAGE_STATUSES.includes(v.status) && v.status !== 'REGISTERED');

  return (
    <div className="split split--detail-right">
      <Card flush title="Triage queue" subtitle={`${waiting.length} waiting for vitals`}>
        {waiting.length === 0 && others.length === 0 ? (
          <EmptyState icon="heart" title="Nobody waiting">Patients appear here after their visit is opened.</EmptyState>
        ) : (
          <div className="queue">
            {waiting.map((v) => <QueueRow key={v.id} visit={v} selected={v.id === selectedId} onSelect={onSelect} />)}
            {others.length > 0 && <div className="queue__divider">Already triaged</div>}
            {others.map((v) => <QueueRow key={v.id} visit={v} selected={v.id === selectedId} onSelect={onSelect} />)}
          </div>
        )}
      </Card>

      <div className="sticky">
        {selectedId ? (
          <VitalsForm key={selectedId} visitId={selectedId} onSaved={onSaved} />
        ) : (
          <Card title="Record vitals">
            <EmptyState icon="heart" title="Select a patient">Choose a patient from the triage queue.</EmptyState>
          </Card>
        )}
      </div>
    </div>
  );
}

function QueueRow({ visit, selected, onSelect }) {
  return (
    <button type="button" className="queue__item" aria-pressed={selected} onClick={() => onSelect(visit.id)}>
      <span className="token">{visit.tokenNumber}</span>
      <span>
        <span className="queue__name">{visit.patient.fullName}</span>{' '}
        <span className="queue__meta">· {ageSex(visit.patient)}</span>
        <span className="queue__complaint">{visit.chiefComplaint}</span>
        <span className="queue__badges">
          <StatusBadge map={VISIT_STATUS} value={visit.status} />
          {visit.priority !== 'NORMAL' && <StatusBadge map={PRIORITY} value={visit.priority} />}
        </span>
      </span>
      <span className="queue__time">{elapsedSince(visit.registeredAt)}</span>
    </button>
  );
}

function VitalsForm({ visitId, onSaved }) {
  const { data: visit, loading, error } = useAsync(() => visitApi.get(visitId), [visitId]);
  const [values, setValues] = useState({});
  const [priority, setPriority] = useState(null);
  const [notice, setNotice] = useState(null);
  const save = useAction();
  const revealRef = useRevealOnNarrow(!loading);

  useEffect(() => {
    if (visit) setPriority(visit.priority);
  }, [visit]);

  if (loading) return <Card title="Record vitals"><p className="muted">Loading…</p></Card>;
  if (error) return <Card title="Record vitals"><Alert tone="error">{error}</Alert></Card>;

  const age = visit.patient.ageYears;
  const previews = Object.fromEntries(VITALS.map((v) => [v.key, previewVitalFlag(v.key, values[v.key], age)]));
  const anyCritical = Object.values(previews).some(isCritical);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = Object.fromEntries(VITALS.map((v) => [v.key, values[v.key] === '' || values[v.key] == null ? null : Number(values[v.key])]));
    payload.priority = priority;
    const updated = await save.run(() => visitApi.recordVitals(visitId, payload), 'Could not save vitals.');
    if (updated) {
      setValues({});
      const raised = updated.priority !== priority;
      setNotice(raised
        ? `Vitals saved. Priority raised to ${PRIORITY[updated.priority].label} because of critical values.`
        : 'Vitals saved. The patient is now in the doctor\'s queue.');
      onSaved(updated);
    }
  };

  return (
    <Card title="Record vitals" subtitle={`Token ${visit.tokenNumber} · ${visit.chiefComplaint}`}>
      <form className="form-grid" onSubmit={handleSubmit} ref={revealRef}>
        <PatientBanner patient={visit.patient} />

        {notice && <Alert tone="success" onClose={() => setNotice(null)}>{notice}</Alert>}
        {save.error && <Alert tone="error" onClose={() => save.setError('')}>{save.error}</Alert>}

        {visit.vitals.length > 0 && (
          <div>
            <div className="field__label section-label">Last recorded</div>
            <VitalsSummary vitals={visit.vitals[0]} compact />
          </div>
        )}

        <div className="vitals-form">
          {VITALS.map((v) => (
            <Field key={v.key} label={`${v.label} (${v.unit})`}>
              {(id) => (
                <div className={`input-flag ${previews[v.key] && previews[v.key] !== 'NORMAL' ? 'input-flag--abnormal' : ''}`}>
                  <input
                    id={id}
                    className="input"
                    type="number"
                    inputMode="decimal"
                    step={v.step}
                    min={v.min}
                    max={v.max}
                    value={values[v.key] ?? ''}
                    onChange={(e) => setValues((s) => ({ ...s, [v.key]: e.target.value }))}
                  />
                  <FlagMark flag={previews[v.key]} />
                </div>
              )}
            </Field>
          ))}
        </div>

        {anyCritical && (
          <Alert tone="error">Critical value entered. Inform the doctor now; the patient will be marked at least Urgent.</Alert>
        )}

        <fieldset className="fieldset-reset">
          <legend className="field__label section-label">Triage priority</legend>
          <div className="segmented">
            {Object.entries(PRIORITY).map(([key, m]) => (
              <label key={key} className={`segmented__option segmented__option--${m.tone}`}>
                <input type="radio" name="triage-priority" value={key} checked={priority === key} onChange={() => setPriority(key)} />
                {m.label}
              </label>
            ))}
          </div>
        </fieldset>

        <Button type="submit" icon="check" block loading={save.busy}>Save vitals</Button>
      </form>
    </Card>
  );
}
