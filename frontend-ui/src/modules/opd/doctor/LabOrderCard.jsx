import { useState } from 'react';
import { Alert, Badge, Button, Icon } from '../../../components/ui/index.jsx';
import { LabResultsTable, StatusBadge } from '../../../components/clinical/index.jsx';
import { LAB_PRIORITY, LAB_STATUS, formatTime } from '../../../domain/clinical.js';
import { labApi } from '../../../api/labApi.js';
import { apiErrorMessage } from '../../../api/errors.js';

/** One lab order as the doctor sees it: progress until released, then flagged results and the PDF. */
export default function LabOrderCard({ order, defaultOpen }) {
  const [open, setOpen] = useState(defaultOpen ?? order.status === 'RELEASED');
  const [error, setError] = useState('');

  const openFile = async (fn) => {
    setError('');
    try {
      await fn();
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not open the file.'));
    }
  };

  const released = order.status === 'RELEASED';

  return (
    <div className={`lab-card ${order.criticalCount ? 'lab-card--critical' : ''}`}>
      <button type="button" className="lab-card__header" onClick={() => setOpen((v) => !v)} aria-expanded={open}>
        <span>
          <span className="cell-primary">{order.items.map((i) => i.testName).join(', ')}</span>
          <span className="cell-secondary"> · <span className="mono">{order.orderNumber}</span> · {formatTime(order.orderedAt)}</span>
        </span>
        <span className="badge-stack">
          {order.priority !== 'ROUTINE' && <StatusBadge map={LAB_PRIORITY} value={order.priority} />}
          {released && order.criticalCount > 0 && <Badge tone="danger">{order.criticalCount} critical</Badge>}
          {released && order.abnormalCount > 0 && order.abnormalCount !== order.criticalCount && (
            <Badge tone="warning">{order.abnormalCount} abnormal</Badge>
          )}
          {order.amended && <Badge tone="neutral">Amended</Badge>}
          <StatusBadge map={LAB_STATUS} value={order.status} />
        </span>
      </button>

      {open && (
        <div className="lab-card__body">
          {error && <Alert tone="error">{error}</Alert>}
          {!released ? (
            <p className="muted">
              {order.status === 'CANCELLED'
                ? `Cancelled: ${order.cancelReason}`
                : 'Results are shown here once the laboratory releases the report.'}
            </p>
          ) : (
            <>
              {order.criticalNotifiedTo && (
                <Alert tone="error">Critical values were phoned to {order.criticalNotifiedTo}.</Alert>
              )}
              <LabResultsTable order={order} />
              <div className="button-row button-row--start">
                <Button size="sm" variant="secondary" icon="file" onClick={() => openFile(() => labApi.openReport(order.id))}>
                  PDF report
                </Button>
                {order.attachments.map((a) => (
                  <Button key={a.id} size="sm" variant="secondary" icon="file"
                    onClick={() => openFile(() => labApi.openAttachment(order.id, a.id))}>
                    {a.description ?? a.originalFilename}
                  </Button>
                ))}
              </div>
              <p className="cell-secondary">
                <Icon name="check" size={13} /> Released by {order.releasedByName} · {formatTime(order.releasedAt)}
              </p>
            </>
          )}
        </div>
      )}
    </div>
  );
}
