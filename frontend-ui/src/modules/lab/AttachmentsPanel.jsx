import { useRef, useState } from 'react';
import { Alert, Button, Field, Icon } from '../../components/ui/index.jsx';
import { formatTime } from '../../domain/clinical.js';
import { labApi } from '../../api/labApi.js';
import { apiErrorMessage } from '../../api/errors.js';
import useAction from '../../hooks/useAction.js';

const MAX_MB = 10;

/** Upload scanned reports, analyser printouts or images (PDF, PNG, JPEG). Removable until release. */
export default function AttachmentsPanel({ order, onChange }) {
  const [file, setFile] = useState(null);
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');
  const inputRef = useRef(null);
  const upload = useAction();
  const remove = useAction();
  const released = order.status === 'RELEASED';

  const pick = (e) => {
    const f = e.target.files?.[0] ?? null;
    setError('');
    if (f && f.size > MAX_MB * 1024 * 1024) {
      setError(`The file is larger than ${MAX_MB} MB.`);
      setFile(null);
      return;
    }
    setFile(f);
  };

  const submit = async (e) => {
    e.preventDefault();
    if (!file) return;
    const added = await upload.run(() => labApi.uploadAttachment(order.id, file, description.trim()), 'Upload failed.');
    if (added) {
      onChange({ ...order, attachments: [...order.attachments, added] });
      setFile(null);
      setDescription('');
      if (inputRef.current) inputRef.current.value = '';
    }
  };

  const del = async (a) => {
    const ok = await remove.run(() => labApi.deleteAttachment(order.id, a.id), 'Could not remove the file.');
    if (ok !== undefined) onChange({ ...order, attachments: order.attachments.filter((x) => x.id !== a.id) });
  };

  const open = async (a) => {
    try {
      await labApi.openAttachment(order.id, a.id);
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not open the file.'));
    }
  };

  return (
    <div className="form-grid">
      {(error || upload.error || remove.error) && <Alert tone="error">{error || upload.error || remove.error}</Alert>}

      {order.attachments.length > 0 ? (
        <ul className="files">
          {order.attachments.map((a) => (
            <li key={a.id} className="files__item">
              <Icon name="file" size={16} />
              <button type="button" className="link-button" onClick={() => open(a)}>{a.description ?? a.originalFilename}</button>
              <span className="cell-secondary">{(a.sizeBytes / 1024).toFixed(0)} KB · {a.uploadedBy} · {formatTime(a.uploadedAt)}</span>
              {!released && (
                <button type="button" className="icon-btn icon-btn--light" aria-label={`Remove ${a.originalFilename}`} onClick={() => del(a)}>
                  <Icon name="trash" size={15} />
                </button>
              )}
            </li>
          ))}
        </ul>
      ) : (
        <p className="muted">No files attached.</p>
      )}

      {order.status !== 'CANCELLED' && (
        <form className="upload" onSubmit={submit}>
          <Field label="File" hint={`PDF, PNG or JPEG, up to ${MAX_MB} MB.`}>
            {(id) => <input id={id} ref={inputRef} className="input" type="file" accept="application/pdf,image/png,image/jpeg" onChange={pick} />}
          </Field>
          <Field label="Description">
            {(id) => <input id={id} className="input" placeholder="e.g. Analyser printout, X-ray film" value={description}
              onChange={(e) => setDescription(e.target.value)} />}
          </Field>
          <Button type="submit" variant="secondary" icon="upload" disabled={!file} loading={upload.busy}>Upload</Button>
        </form>
      )}
    </div>
  );
}
