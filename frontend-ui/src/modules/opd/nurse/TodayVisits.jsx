import { useMemo, useState } from 'react';
import { Alert, Button, Card, Dialog, EmptyState, Field } from '../../../components/ui/index.jsx';
import { StatusBadge } from '../../../components/clinical/index.jsx';
import { PRIORITY, VISIT_STATUS, ageSex, formatTime } from '../../../domain/clinical.js';
import { visitApi } from '../../../api/opdApi.js';
import useAction from '../../../hooks/useAction.js';

const CANCELLABLE = ['REGISTERED', 'TRIAGED'];

/** All of today's visits with search, status filter and cancellation (before the doctor sees them). */
export default function TodayVisits({ visits, onChanged }) {
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('ALL');
  const [cancelling, setCancelling] = useState(null);

  const rows = useMemo(() => {
    const q = query.trim().toLowerCase();
    return visits
      .filter((v) => status === 'ALL' || v.status === status)
      .filter((v) => !q || [v.patient.fullName, v.patient.mrn, v.patient.phone, String(v.tokenNumber)]
        .some((x) => x?.toLowerCase().includes(q)));
  }, [visits, query, status]);

  return (
    <Card flush title="Today's visits" subtitle={`${rows.length} of ${visits.length} shown`}>
      <div className="card__body toolbar">
        <label className="sr-only" htmlFor="visit-search">Search visits</label>
        <input id="visit-search" className="input" type="search" placeholder="Search name, MRN, phone or token"
          value={query} onChange={(e) => setQuery(e.target.value)} />
        <label className="sr-only" htmlFor="visit-status">Filter by status</label>
        <select id="visit-status" className="input toolbar__select" value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="ALL">All statuses</option>
          {Object.entries(VISIT_STATUS).map(([k, m]) => <option key={k} value={k}>{m.label}</option>)}
        </select>
      </div>

      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr>
              <th scope="col">Token</th>
              <th scope="col">Patient</th>
              <th scope="col" className="col-grow">Complaint</th>
              <th scope="col" className="col-hide-sm">Doctor</th>
              <th scope="col" className="col-hide-sm">Arrived</th>
              <th scope="col">Status</th>
              <th scope="col"><span className="sr-only">Actions</span></th>
            </tr>
          </thead>
          <tbody>
            {rows.map((v) => (
              <tr key={v.id}>
                <td className="mono">{v.tokenNumber}</td>
                <td>
                  <div className="cell-primary">{v.patient.fullName}</div>
                  <div className="cell-secondary">{v.patient.mrn} · {ageSex(v.patient)}</div>
                </td>
                <td className="col-grow"><div className="truncate" title={v.chiefComplaint}>{v.chiefComplaint}</div></td>
                <td className="cell-secondary col-hide-sm">{v.assignedDoctorName ?? '–'}</td>
                <td className="cell-secondary col-hide-sm">{formatTime(v.registeredAt)}</td>
                <td>
                  <div className="badge-stack">
                    <StatusBadge map={VISIT_STATUS} value={v.status} />
                    {v.priority !== 'NORMAL' && <StatusBadge map={PRIORITY} value={v.priority} />}
                  </div>
                </td>
                <td className="cell-action">
                  {CANCELLABLE.includes(v.status) && (
                    <Button size="sm" variant="secondary" onClick={() => setCancelling(v)}>Cancel</Button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {rows.length === 0 && (
          <EmptyState icon="users" title={visits.length ? 'No matching visits' : 'No visits yet today'}>
            {visits.length ? 'Try another search or status.' : 'Open a visit from the Register tab.'}
          </EmptyState>
        )}
      </div>

      {cancelling && (
        <CancelVisitDialog visit={cancelling} onClose={() => setCancelling(null)} onDone={() => { setCancelling(null); onChanged(); }} />
      )}
    </Card>
  );
}

function CancelVisitDialog({ visit, onClose, onDone }) {
  const [reason, setReason] = useState('');
  const cancel = useAction();

  const submit = async (e) => {
    e.preventDefault();
    if (await cancel.run(() => visitApi.cancel(visit.id, reason.trim()), 'Could not cancel the visit.')) onDone();
  };

  return (
    <Dialog title={`Cancel token ${visit.tokenNumber}?`} onClose={onClose}>
      <form className="form-grid" onSubmit={submit}>
        <p>{visit.patient.fullName} will be removed from today's queue. The record is kept.</p>
        {cancel.error && <Alert tone="error">{cancel.error}</Alert>}
        <Field label="Reason">
          {(id) => <input id={id} className="input" required autoFocus placeholder="e.g. Left without being seen"
            value={reason} onChange={(e) => setReason(e.target.value)} />}
        </Field>
        <div className="button-row">
          <Button type="button" variant="secondary" onClick={onClose}>Keep visit</Button>
          <Button type="submit" variant="danger" loading={cancel.busy}>Cancel visit</Button>
        </div>
      </form>
    </Dialog>
  );
}
