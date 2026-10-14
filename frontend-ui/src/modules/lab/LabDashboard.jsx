import { useState } from 'react';
import AppShell from '../../components/layout/AppShell.jsx';
import { Alert, Badge, Button, Card, EmptyState, Skeleton, Tabs } from '../../components/ui/index.jsx';
import { StatusBadge } from '../../components/clinical/index.jsx';
import { LAB_PRIORITY, LAB_STATUS, elapsedSince, formatTime, genderShort } from '../../domain/clinical.js';
import { labApi } from '../../api/labApi.js';
import useAsync from '../../hooks/useAsync.js';
import useInterval from '../../hooks/useInterval.js';
import OrderWorkspace from './OrderWorkspace.jsx';

const TABS = {
  collect: { label: 'To collect', statuses: ['ORDERED'] },
  progress: { label: 'In progress', statuses: ['SAMPLE_COLLECTED', 'IN_PROGRESS'] },
  done: { label: 'Released (24 h)', statuses: ['RELEASED', 'CANCELLED'] },
};
const REFRESH_MS = 30_000;

export default function LabDashboard() {
  const [tab, setTab] = useState('collect');
  const [selectedId, setSelectedId] = useState(null);
  const open = useAsync(() => labApi.worklist(['ORDERED', 'SAMPLE_COLLECTED', 'IN_PROGRESS']), [], { initial: [] });
  const done = useAsync(() => labApi.worklist(TABS.done.statuses), [], { initial: [] });

  const refresh = (opts) => { open.reload(opts); done.reload(opts); };
  useInterval(() => refresh({ silent: true }), REFRESH_MS);

  const rows = tab === 'done' ? done.data : open.data.filter((o) => TABS[tab].statuses.includes(o.status));
  const counts = {
    collect: open.data.filter((o) => o.status === 'ORDERED').length,
    progress: open.data.filter((o) => o.status !== 'ORDERED').length,
    done: done.data.length,
  };
  const stat = open.data.filter((o) => o.priority === 'STAT').length;
  const loading = tab === 'done' ? done.loading : open.loading;

  return (
    <AppShell
      title="Laboratory"
      subtitle={stat ? `${stat} STAT order${stat > 1 ? 's' : ''} open` : 'Worklist, most urgent first'}
      actions={<Button variant="secondary" icon="refresh" onClick={() => refresh()} loading={open.loading}>Refresh</Button>}
    >
      {(open.error || done.error) && <Alert tone="error">{open.error || done.error}</Alert>}

      <Tabs label="Worklist" value={tab} onChange={(t) => { setTab(t); setSelectedId(null); }}
        tabs={Object.entries(TABS).map(([id, t]) => ({ id, label: t.label, count: counts[id] }))} />

      <div className="split split--queue">
        <Card flush className="sticky" title={TABS[tab].label}>
          {loading && !rows.length ? (
            <div className="card__body form-grid">{[1, 2, 3].map((i) => <Skeleton key={i} />)}</div>
          ) : rows.length === 0 ? (
            <EmptyState icon="flask" title="Nothing here">New orders from doctors appear under “To collect”.</EmptyState>
          ) : (
            <div className="queue" role="list">
              {rows.map((o) => (
                <button key={o.id} type="button" role="listitem"
                  className={`queue__item ${o.priority === 'STAT' ? 'queue__item--emergency' : ''}`}
                  aria-pressed={o.id === selectedId} onClick={() => setSelectedId(o.id)}>
                  <span className="token token--wide mono">{o.orderNumber.slice(-5)}</span>
                  <span>
                    <span className="queue__name">{o.patientName}</span>{' '}
                    <span className="queue__meta">· {o.patientAgeYears} y · {genderShort(o.patientGender)}</span>
                    <span className="queue__complaint">{o.testNames.join(', ')}</span>
                    <span className="queue__badges">
                      {o.priority !== 'ROUTINE' && <StatusBadge map={LAB_PRIORITY} value={o.priority} />}
                      {tab !== 'collect' && <StatusBadge map={LAB_STATUS} value={o.status} />}
                      {o.status === 'IN_PROGRESS' && <Badge tone="neutral">{o.resultsEntered}/{o.resultsTotal}</Badge>}
                      {o.criticalCount > 0 && <Badge tone="danger">{o.criticalCount} critical</Badge>}
                    </span>
                  </span>
                  <span className="queue__time">{tab === 'done' ? formatTime(o.releasedAt ?? o.orderedAt) : elapsedSince(o.orderedAt)}</span>
                </button>
              ))}
            </div>
          )}
        </Card>

        {selectedId ? (
          <OrderWorkspace key={selectedId} orderId={selectedId} onChanged={() => refresh({ silent: true })} />
        ) : (
          <Card title="No order open">
            <EmptyState icon="flask" title="Select an order">Collect the sample, enter results and release the report.</EmptyState>
          </Card>
        )}
      </div>
    </AppShell>
  );
}
