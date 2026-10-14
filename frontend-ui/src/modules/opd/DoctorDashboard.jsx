import { useContext, useState } from 'react';
import { AuthContext } from '../../context/AuthContext.jsx';
import AppShell from '../../components/layout/AppShell.jsx';
import { Alert, Button, Card, EmptyState, Skeleton } from '../../components/ui/index.jsx';
import { StatusBadge } from '../../components/clinical/index.jsx';
import { PRIORITY, VISIT_STATUS, ageSex, elapsedSince } from '../../domain/clinical.js';
import { visitApi } from '../../api/opdApi.js';
import { labApi } from '../../api/labApi.js';
import useAsync from '../../hooks/useAsync.js';
import useInterval from '../../hooks/useInterval.js';
import ConsultationWorkspace from './doctor/ConsultationWorkspace.jsx';

const REFRESH_MS = 30_000;

export default function DoctorDashboard() {
  const { user } = useContext(AuthContext);
  const queue = useAsync(() => visitApi.doctorQueue(), [], { initial: [] });
  const catalog = useAsync(() => labApi.catalog(), [], { initial: [] });
  const [selectedId, setSelectedId] = useState(null);

  // Nurses keep adding patients and the lab keeps releasing results.
  useInterval(() => queue.reload({ silent: true }), REFRESH_MS);

  const mine = queue.data.filter((v) => v.assignedDoctor === user.username && v.status === 'IN_CONSULTATION');
  const waiting = queue.data.filter((v) => !mine.includes(v));

  return (
    <AppShell
      title="Consultation"
      subtitle={queue.loading && !queue.data.length ? 'Loading queue…' : `${waiting.length} waiting · emergencies and urgent cases first`}
      actions={<Button variant="secondary" icon="refresh" onClick={() => queue.reload()} loading={queue.loading}>Refresh</Button>}
    >
      {queue.error && <Alert tone="error">{queue.error}</Alert>}
      {catalog.error && <Alert tone="error">Lab catalog unavailable: {catalog.error}</Alert>}

      <div className="split split--queue">
        <div className="sticky form-grid">
          {mine.length > 0 && (
            <Card flush title="With you now">
              <QueueList visits={mine} selectedId={selectedId} onSelect={setSelectedId} user={user} />
            </Card>
          )}
          <Card flush title="Queue" subtitle="Select a patient to open their file.">
            {queue.loading && !queue.data.length ? (
              <div className="card__body form-grid">{[1, 2, 3].map((i) => <Skeleton key={i} />)}</div>
            ) : waiting.length === 0 ? (
              <EmptyState icon="users" title="Queue is clear">Triaged patients appear here automatically.</EmptyState>
            ) : (
              <QueueList visits={waiting} selectedId={selectedId} onSelect={setSelectedId} user={user} numbered />
            )}
          </Card>
        </div>

        {selectedId ? (
          <ConsultationWorkspace
            key={selectedId}
            visitId={selectedId}
            catalog={catalog.data}
            onChanged={() => queue.reload({ silent: true })}
          />
        ) : (
          <Card title="No patient open">
            <EmptyState icon="stethoscope" title="Pick a patient from the queue">
              You will see their vitals, history and lab results, and can write notes and order tests.
            </EmptyState>
          </Card>
        )}
      </div>
    </AppShell>
  );
}

function QueueList({ visits, selectedId, onSelect, numbered, user }) {
  return (
    <div className="queue" role="list">
      {visits.map((v, i) => (
        <button
          key={v.id}
          type="button"
          role="listitem"
          className={`queue__item ${v.priority === 'EMERGENCY' ? 'queue__item--emergency' : ''}`}
          aria-pressed={v.id === selectedId}
          onClick={() => onSelect(v.id)}
        >
          <span className="token" title={`Token ${v.tokenNumber}`}>{numbered ? i + 1 : v.tokenNumber}</span>
          <span>
            <span className="queue__name">{v.patient.fullName}</span>{' '}
            <span className="queue__meta">· {ageSex(v.patient)}</span>
            <span className="queue__complaint">{v.chiefComplaint}</span>
            <span className="queue__badges">
              {v.priority !== 'NORMAL' && <StatusBadge map={PRIORITY} value={v.priority} />}
              {v.status !== 'TRIAGED' && <StatusBadge map={VISIT_STATUS} value={v.status} />}
              {v.latestVitals?.hasCritical && <span className="badge badge--danger">Critical vitals</span>}
              {v.patient.allergies && <span className="badge badge--warning">Allergies</span>}
              {v.assignedDoctor && v.assignedDoctor !== user.username && (
                <span className="badge badge--neutral">{v.assignedDoctorName}</span>
              )}
            </span>
          </span>
          <span className="queue__time">{elapsedSince(v.registeredAt)}</span>
        </button>
      ))}
    </div>
  );
}
