import { useState } from 'react';
import { Alert, Button, Card, Dialog, Field, Icon } from '../../components/ui/index.jsx';
import { LabResultsTable, StatusBadge } from '../../components/clinical/index.jsx';
import { FLAG, LAB_PRIORITY, LAB_STATUS, formatTime, genderShort, previewResultFlag } from '../../domain/clinical.js';
import { labApi } from '../../api/labApi.js';
import { apiErrorMessage } from '../../api/errors.js';
import useAsync from '../../hooks/useAsync.js';
import useAction from '../../hooks/useAction.js';
import useRevealOnNarrow from '../../hooks/useRevealOnNarrow.js';
import ResultEntryForm from './ResultEntryForm.jsx';
import AttachmentsPanel from './AttachmentsPanel.jsx';

/** One order through its lab life: collect sample → enter results → release → amend if needed. */
export default function OrderWorkspace({ orderId, onChanged }) {
  const { data: order, setData, loading, error } = useAsync(() => labApi.get(orderId), [orderId]);
  const [cancelling, setCancelling] = useState(false);
  const [amending, setAmending] = useState(null);
  const [fileError, setFileError] = useState('');
  const collect = useAction();
  const revealRef = useRevealOnNarrow(!loading);

  if (loading) return <Card title="Order"><p className="muted">Loading…</p></Card>;
  if (error) return <Card title="Order"><Alert tone="error">{error}</Alert></Card>;

  const update = (next) => {
    setData(next);
    onChanged();
  };

  const preview = async () => {
    setFileError('');
    try {
      await labApi.openReport(order.id);
    } catch (err) {
      setFileError(apiErrorMessage(err, 'Could not open the report.'));
    }
  };

  const canCancel = ['ORDERED', 'SAMPLE_COLLECTED', 'IN_PROGRESS'].includes(order.status);
  const entering = ['SAMPLE_COLLECTED', 'IN_PROGRESS'].includes(order.status);

  return (
    <div className="workspace" ref={revealRef}>
      <Card>
        <div className="order-head">
          <div>
            <div className="patient-banner__name">{order.patientName}</div>
            <div className="patient-banner__meta">
              <span className="mono">{order.mrn}</span>
              <span>{order.patientAgeYears} y · {genderShort(order.patientGender)}</span>
              <span>Ref. {order.orderingDoctorName ?? order.orderingDoctor}</span>
            </div>
          </div>
          <div className="badge-stack">
            {order.priority !== 'ROUTINE' && <StatusBadge map={LAB_PRIORITY} value={order.priority} />}
            <StatusBadge map={LAB_STATUS} value={order.status} />
          </div>
        </div>
        <div className="workspace__meta">
          <span className="mono"><Icon name="flask" size={14} /> {order.orderNumber}</span>
          <span><Icon name="clock" size={14} /> Ordered {formatTime(order.orderedAt)}</span>
          {order.sampleCollectedAt && <span>Collected {formatTime(order.sampleCollectedAt)} by {order.sampleCollectedBy}</span>}
        </div>
        {order.clinicalNotes && <div className="detail__block"><strong>Clinical notes:</strong> {order.clinicalNotes}</div>}
        {fileError && <Alert tone="error">{fileError}</Alert>}
        {collect.error && <Alert tone="error">{collect.error}</Alert>}

        <div className="button-row button-row--start">
          {order.status === 'ORDERED' && (
            <Button icon="check" loading={collect.busy}
              onClick={() => collect.run(() => labApi.collectSample(order.id), 'Could not update the order.').then((o) => o && update(o))}>
              Sample collected
            </Button>
          )}
          {order.status !== 'CANCELLED' && order.status !== 'ORDERED' && (
            <Button variant="secondary" icon="file" onClick={preview}>
              {order.status === 'RELEASED' ? 'PDF report' : 'Preview report'}
            </Button>
          )}
          {canCancel && <Button variant="secondary" onClick={() => setCancelling(true)}>Cancel order</Button>}
        </div>
        {order.status === 'ORDERED' && (
          <p className="field__hint">Label the sample with <span className="mono">{order.orderNumber}</span>: {order.items.map((i) => i.specimen).filter(Boolean).join(', ')}.</p>
        )}
      </Card>

      {entering && (
        <Card title="Enter results" subtitle="Values outside the reference range turn red; critical values must be phoned before release.">
          <ResultEntryForm key={order.id} order={order} onChange={update} />
        </Card>
      )}

      {order.status === 'RELEASED' && (
        <Card title="Released results" subtitle={`Released ${formatTime(order.releasedAt)} by ${order.releasedByName}`}>
          {order.criticalNotifiedTo && <Alert tone="error">Critical values phoned to {order.criticalNotifiedTo}.</Alert>}
          <LabResultsTable
            order={order}
            renderAction={(r) => (
              <Button size="sm" variant="secondary" icon="edit" onClick={() => setAmending(r)}>Amend</Button>
            )}
          />
        </Card>
      )}

      {order.status === 'CANCELLED' && (
        <Card title="Cancelled"><p>{order.cancelReason}</p></Card>
      )}

      {order.status !== 'ORDERED' && (
        <Card title="Attachments" subtitle="Scanned reports, analyser printouts, images.">
          <AttachmentsPanel order={order} onChange={setData} />
        </Card>
      )}

      {cancelling && (
        <ReasonDialog
          title={`Cancel ${order.orderNumber}?`}
          intro="The doctor will see this order as cancelled. Use this for rejected or haemolysed samples, or orders placed in error."
          placeholder="e.g. Sample haemolysed, recollection requested"
          confirm="Cancel order"
          onClose={() => setCancelling(false)}
          onSubmit={(reason) => labApi.cancel(order.id, reason)}
          onDone={(o) => { setCancelling(false); update(o); }}
        />
      )}

      {amending && (
        <AmendDialog order={order} result={amending} onClose={() => setAmending(null)}
          onDone={(o) => { setAmending(null); update(o); }} />
      )}
    </div>
  );
}

function ReasonDialog({ title, intro, placeholder, confirm, onClose, onSubmit, onDone }) {
  const [reason, setReason] = useState('');
  const act = useAction();
  const submit = async (e) => {
    e.preventDefault();
    const result = await act.run(() => onSubmit(reason.trim()));
    if (result) onDone(result);
  };
  return (
    <Dialog title={title} onClose={onClose}>
      <form className="form-grid" onSubmit={submit}>
        <p>{intro}</p>
        {act.error && <Alert tone="error">{act.error}</Alert>}
        <Field label="Reason">
          {(id) => <input id={id} className="input" required autoFocus placeholder={placeholder} value={reason} onChange={(e) => setReason(e.target.value)} />}
        </Field>
        <div className="button-row">
          <Button type="button" variant="secondary" onClick={onClose}>Back</Button>
          <Button type="submit" variant="danger" loading={act.busy}>{confirm}</Button>
        </div>
      </form>
    </Dialog>
  );
}

function AmendDialog({ order, result, onClose, onDone }) {
  const [value, setValue] = useState(result.value ?? '');
  const [reason, setReason] = useState('');
  const act = useAction();
  const flag = previewResultFlag(result, value);

  const submit = async (e) => {
    e.preventDefault();
    const updated = await act.run(() => labApi.amend(order.id, result.id, value, reason.trim()), 'Could not amend the result.');
    if (updated) onDone(updated);
  };

  return (
    <Dialog title={`Amend ${result.parameterName}`} onClose={onClose}>
      <form className="form-grid" onSubmit={submit}>
        <p>The released value <strong>{result.value}{result.unit ? ` ${result.unit}` : ''}</strong> will be replaced. The old value, new value and reason stay on the report.</p>
        {act.error && <Alert tone="error">{act.error}</Alert>}
        <Field label={`Corrected value${result.unit ? ` (${result.unit})` : ''}`} hint={result.referenceText ? `Reference: ${result.referenceText}` : undefined}>
          {(id) => (result.valueType === 'NARRATIVE'
            ? <textarea id={id} className="input" required value={value} onChange={(e) => setValue(e.target.value)} />
            : <input id={id} className="input" required autoFocus value={value} onChange={(e) => setValue(e.target.value)} />)}
        </Field>
        {flag && flag !== 'NONE' && flag !== 'NORMAL' && (
          <Alert tone="error">{flag === 'INVALID' ? 'Not a number.' : `New value is ${FLAG[flag].label.toLowerCase()}. Inform the doctor of the change.`}</Alert>
        )}
        <Field label="Reason for amendment">
          {(id) => <input id={id} className="input" required placeholder="e.g. Transcription error, repeat analysis" value={reason} onChange={(e) => setReason(e.target.value)} />}
        </Field>
        <div className="button-row">
          <Button type="button" variant="secondary" onClick={onClose}>Back</Button>
          <Button type="submit" icon="check" loading={act.busy} disabled={flag === 'INVALID'}>Save amendment</Button>
        </div>
      </form>
    </Dialog>
  );
}
