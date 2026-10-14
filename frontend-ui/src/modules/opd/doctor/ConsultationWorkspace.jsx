import { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../../../context/AuthContext.jsx';
import { Alert, Button, Card, Field, Icon } from '../../../components/ui/index.jsx';
import { PatientBanner, StatusBadge, VitalsSummary } from '../../../components/clinical/index.jsx';
import { PRIORITY, VISIT_STATUS, formatDate, formatTime } from '../../../domain/clinical.js';
import { patientApi, visitApi } from '../../../api/opdApi.js';
import { labApi } from '../../../api/labApi.js';
import useAsync from '../../../hooks/useAsync.js';
import useAction from '../../../hooks/useAction.js';
import useRevealOnNarrow from '../../../hooks/useRevealOnNarrow.js';
import LabOrderCard from './LabOrderCard.jsx';
import LabOrderForm from './LabOrderForm.jsx';

const STARTABLE = ['REGISTERED', 'TRIAGED', 'AWAITING_LAB', 'LAB_RESULTS_READY'];

/** Everything for one visit: identity, vitals, notes and diagnosis, lab orders and results, history. */
export default function ConsultationWorkspace({ visitId, catalog, onChanged }) {
  const { user } = useContext(AuthContext);
  const { data: visit, setData: setVisit, loading, error } = useAsync(() => visitApi.get(visitId), [visitId]);
  const labs = useAsync(() => labApi.byVisit(visitId), [visitId], { initial: [] });
  const action = useAction();
  const revealRef = useRevealOnNarrow(!loading);

  if (loading) return <Card title="Consultation"><p className="muted">Loading…</p></Card>;
  if (error) return <Card title="Consultation"><Alert tone="error">{error}</Alert></Card>;

  const mine = visit.status === 'IN_CONSULTATION' && visit.assignedDoctor === user.username;
  const withOther = visit.status === 'IN_CONSULTATION' && !mine;
  const closed = ['COMPLETED', 'CANCELLED'].includes(visit.status);

  const apply = (updated) => {
    if (updated) {
      setVisit(updated);
      onChanged();
    }
    return updated;
  };

  const start = () => action.run(() => visitApi.startConsultation(visit.id), 'Could not start the consultation.').then(apply);

  return (
    <div className="workspace" ref={revealRef}>
      <Card>
        <PatientBanner patient={visit.patient}>
          <div className="badge-stack">
            <StatusBadge map={VISIT_STATUS} value={visit.status} />
            {visit.priority !== 'NORMAL' && <StatusBadge map={PRIORITY} value={visit.priority} />}
          </div>
        </PatientBanner>

        <div className="workspace__meta">
          <span><Icon name="clipboard" size={14} /> Token {visit.tokenNumber} · arrived {formatTime(visit.registeredAt)}</span>
          {visit.assignedDoctorName && <span><Icon name="stethoscope" size={14} /> {visit.assignedDoctorName}</span>}
        </div>

        {action.error && <Alert tone="error" onClose={() => action.setError('')}>{action.error}</Alert>}

        {STARTABLE.includes(visit.status) && (
          <Button icon="play" onClick={start} loading={action.busy}>
            {visit.assignedDoctor ? 'Resume consultation' : 'Start consultation'}
          </Button>
        )}
        {withOther && <Alert tone="info">This patient is with {visit.assignedDoctorName}.</Alert>}
        {visit.status === 'LAB_RESULTS_READY' && (
          <Alert tone="info">Lab results are ready. Resume the consultation to review them and continue.</Alert>
        )}
      </Card>

      <Card title="Presenting complaint & vitals" subtitle={visit.vitals[0] ? `Recorded ${formatTime(visit.vitals[0].recordedAt)} by ${visit.vitals[0].recordedBy}` : undefined}>
        <p className="complaint">{visit.chiefComplaint}</p>
        <VitalsSummary vitals={visit.vitals[0]} />
      </Card>

      <NotesCard key={`${visit.id}-${visit.status}`} visit={visit} editable={mine} onSaved={apply} />

      <Card title="Laboratory" subtitle={labs.data.length ? `${labs.data.length} order${labs.data.length > 1 ? 's' : ''} for this visit` : 'No tests ordered yet'}
        actions={<Button size="sm" variant="secondary" icon="refresh" onClick={() => labs.reload()} loading={labs.loading}>Refresh</Button>}>
        <div className="form-grid">
          {labs.error && <Alert tone="error">{labs.error}</Alert>}
          {labs.data.map((o) => <LabOrderCard key={o.id} order={o} />)}
          {mine && catalog.length > 0 && (
            <details className="disclosure" open={labs.data.length === 0}>
              <summary>Order tests</summary>
              <LabOrderForm visitId={visit.id} catalog={catalog}
                onOrdered={(updated) => { apply(updated); labs.reload(); }} />
            </details>
          )}
        </div>
      </Card>

      <History patientId={visit.patient.id} currentVisitId={visit.id} />

      {!closed && <Timeline events={visit.events} />}
    </div>
  );
}

function NotesCard({ visit, editable, onSaved }) {
  const [notes, setNotes] = useState(visit.consultationNotes ?? '');
  const [diagnosis, setDiagnosis] = useState(visit.diagnosis ?? '');
  const [saved, setSaved] = useState(false);
  const save = useAction();
  const complete = useAction();

  useEffect(() => setSaved(false), [notes, diagnosis]);

  const dirty = notes !== (visit.consultationNotes ?? '') || diagnosis !== (visit.diagnosis ?? '');

  const persist = () => save.run(
    () => visitApi.saveConsultation(visit.id, { consultationNotes: notes, diagnosis }),
    'Could not save notes.',
  );

  const handleSave = async (e) => {
    e.preventDefault();
    const updated = await persist();
    if (updated) { onSaved(updated); setSaved(true); }
  };

  const handleComplete = async () => {
    if (dirty && !(await persist())) return;
    const updated = await complete.run(() => visitApi.complete(visit.id), 'Could not complete the visit.');
    if (updated) onSaved(updated);
  };

  if (!editable) {
    return (
      <Card title="Consultation notes">
        {visit.diagnosis || visit.consultationNotes ? (
          <dl className="detail detail--list">
            <div><dt className="field__label">Diagnosis</dt><dd className="detail__block">{visit.diagnosis ?? '–'}</dd></div>
            <div><dt className="field__label">Notes</dt><dd className="detail__block pre">{visit.consultationNotes ?? '–'}</dd></div>
          </dl>
        ) : (
          <p className="muted">Notes can be written once you start the consultation.</p>
        )}
      </Card>
    );
  }

  return (
    <Card title="Consultation notes">
      <form className="form-grid" onSubmit={handleSave}>
        {(save.error || complete.error) && <Alert tone="error">{save.error || complete.error}</Alert>}
        {saved && !dirty && <Alert tone="success" onClose={() => setSaved(false)}>Notes saved.</Alert>}
        <Field label="History, examination and plan">
          {(id) => <textarea id={id} className="input input--tall" value={notes} onChange={(e) => setNotes(e.target.value)} />}
        </Field>
        <Field label="Diagnosis" hint="Required to complete the visit.">
          {(id) => <input id={id} className="input" value={diagnosis} onChange={(e) => setDiagnosis(e.target.value)} />}
        </Field>
        <div className="button-row">
          <Button type="submit" variant="secondary" loading={save.busy} disabled={!dirty}>Save notes</Button>
          <Button type="button" icon="check" loading={complete.busy} disabled={!diagnosis.trim()} onClick={handleComplete}>
            Complete visit
          </Button>
        </div>
      </form>
    </Card>
  );
}

function History({ patientId, currentVisitId }) {
  const { data, loading } = useAsync(() => patientApi.history(patientId), [patientId], { initial: [] });
  const previous = data.filter((v) => v.id !== currentVisitId);
  if (loading || previous.length === 0) return null;
  return (
    <Card title="Previous visits">
      <ul className="history">
        {previous.slice(0, 10).map((v) => (
          <li key={v.id}>
            <span className="cell-secondary">{formatDate(v.visitDate)}</span>
            <span className="truncate" title={v.chiefComplaint}>{v.chiefComplaint}</span>
            <StatusBadge map={VISIT_STATUS} value={v.status} />
          </li>
        ))}
      </ul>
    </Card>
  );
}

function Timeline({ events }) {
  return (
    <details className="card disclosure disclosure--card">
      <summary>Visit timeline ({events.length})</summary>
      <ol className="timeline">
        {events.map((e, i) => (
          <li key={i}>
            <span className="cell-secondary">{formatTime(e.occurredAt)} · {e.actor}</span>
            <span>{VISIT_STATUS[e.toStatus]?.label ?? e.toStatus}{e.note ? ` — ${e.note}` : ''}</span>
          </li>
        ))}
      </ol>
    </details>
  );
}
