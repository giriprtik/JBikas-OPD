import { useMemo, useState } from 'react';
import AppShell from '../../components/layout/AppShell.jsx';
import { Alert, Button, Skeleton, Tabs } from '../../components/ui/index.jsx';
import { visitApi } from '../../api/opdApi.js';
import useAsync from '../../hooks/useAsync.js';
import useInterval from '../../hooks/useInterval.js';
import RegisterVisit from './nurse/RegisterVisit.jsx';
import TriagePanel from './nurse/TriagePanel.jsx';
import TodayVisits from './nurse/TodayVisits.jsx';

const REFRESH_MS = 30_000;

export default function NurseDashboard() {
  const { data: visits, loading, error, reload } = useAsync(() => visitApi.today(), [], { initial: [] });
  const [tab, setTab] = useState('register');
  const [triageId, setTriageId] = useState(null);
  const [notice, setNotice] = useState(null);

  useInterval(() => reload({ silent: true }), REFRESH_MS);

  const stats = useMemo(() => {
    const count = (fn) => visits.filter(fn).length;
    return [
      { label: 'Visits today', value: visits.length },
      { label: 'Awaiting triage', value: count((v) => v.status === 'REGISTERED') },
      { label: 'Waiting for doctor', value: count((v) => ['TRIAGED', 'LAB_RESULTS_READY'].includes(v.status)) },
      { label: 'Awaiting lab', value: count((v) => v.status === 'AWAITING_LAB') },
    ];
  }, [visits]);

  const awaitingTriage = stats[1].value;

  const handleOpened = (visit) => {
    setNotice({
      text: `Token ${visit.tokenNumber} issued to ${visit.patient.fullName} (${visit.patient.mrn}).`,
      visitId: visit.id,
    });
    reload({ silent: true });
  };

  return (
    <AppShell
      title="Nurse desk"
      subtitle="Register arrivals, record vitals and keep the OPD moving."
      actions={<Button variant="secondary" icon="refresh" onClick={() => reload()} loading={loading}>Refresh</Button>}
    >
      <div className="stats">
        {stats.map((s) => (
          <div key={s.label} className="card stat">
            <div className="stat__label">{s.label}</div>
            <div className="stat__value">{loading && !visits.length ? <Skeleton width="40px" /> : s.value}</div>
          </div>
        ))}
      </div>

      {notice && (
        <Alert tone="success" onClose={() => setNotice(null)}>
          {notice.text}{' '}
          <button type="button" className="link-button" onClick={() => { setTriageId(notice.visitId); setTab('triage'); setNotice(null); }}>
            Record vitals now
          </button>
        </Alert>
      )}
      {error && <Alert tone="error">{error}</Alert>}

      <Tabs
        label="Nurse desk sections"
        value={tab}
        onChange={setTab}
        tabs={[
          { id: 'register', label: 'Register visit' },
          { id: 'triage', label: 'Triage', count: awaitingTriage },
          { id: 'today', label: "Today's visits", count: visits.length },
        ]}
      />

      {tab === 'register' && <RegisterVisit onVisitOpened={handleOpened} />}
      {tab === 'triage' && (
        <TriagePanel
          visits={visits}
          selectedId={triageId}
          onSelect={setTriageId}
          onSaved={() => reload({ silent: true })}
        />
      )}
      {tab === 'today' && <TodayVisits visits={visits} onChanged={() => reload({ silent: true })} />}
    </AppShell>
  );
}
