import { useMemo, useState } from 'react';
import { Alert, Button, Field } from '../../../components/ui/index.jsx';
import { LAB_PRIORITY } from '../../../domain/clinical.js';
import { visitApi } from '../../../api/opdApi.js';
import useAction from '../../../hooks/useAction.js';

const CATEGORY_LABEL = {
  HEMATOLOGY: 'Haematology', BIOCHEMISTRY: 'Biochemistry', ENDOCRINOLOGY: 'Endocrinology', URINE: 'Urine', RADIOLOGY: 'Radiology',
};

/** Test picker from the lab catalog. The order goes through opd-service so the visit status follows. */
export default function LabOrderForm({ visitId, catalog, onOrdered }) {
  const [selected, setSelected] = useState([]);
  const [priority, setPriority] = useState('ROUTINE');
  const [notes, setNotes] = useState('');
  const order = useAction();

  const groups = useMemo(() => {
    const byCategory = new Map();
    catalog.forEach((t) => {
      if (!byCategory.has(t.category)) byCategory.set(t.category, []);
      byCategory.get(t.category).push(t);
    });
    return [...byCategory.entries()];
  }, [catalog]);

  const toggle = (code) => setSelected((s) => (s.includes(code) ? s.filter((c) => c !== code) : [...s, code]));

  const submit = async (e) => {
    e.preventDefault();
    const visit = await order.run(
      () => visitApi.orderLab(visitId, { testCodes: selected, priority, clinicalNotes: notes.trim() || null }),
      'The lab order could not be placed.',
    );
    if (visit) {
      setSelected([]);
      setNotes('');
      onOrdered(visit);
    }
  };

  return (
    <form className="form-grid" onSubmit={submit}>
      {order.error && <Alert tone="error" onClose={() => order.setError('')}>{order.error}</Alert>}

      {groups.map(([category, tests]) => (
        <fieldset key={category} className="fieldset-reset">
          <legend className="field__label section-label">{CATEGORY_LABEL[category] ?? category}</legend>
          <div className="check-grid">
            {tests.map((t) => (
              <label key={t.code} className="choice">
                <input type="checkbox" checked={selected.includes(t.code)} onChange={() => toggle(t.code)} />
                <span>
                  {t.name}
                  <span className="cell-secondary"> · ~{t.turnaroundHours} h</span>
                </span>
              </label>
            ))}
          </div>
        </fieldset>
      ))}

      <fieldset className="fieldset-reset">
        <legend className="field__label section-label">Priority</legend>
        <div className="segmented">
          {Object.entries(LAB_PRIORITY).reverse().map(([key, m]) => (
            <label key={key} className={`segmented__option segmented__option--${m.tone}`}>
              <input type="radio" name="lab-priority" value={key} checked={priority === key} onChange={() => setPriority(key)} />
              {m.label}
            </label>
          ))}
        </div>
      </fieldset>

      <Field label="Clinical notes for the lab" hint="Shown on the worklist and printed on the report.">
        {(id) => <input id={id} className="input" placeholder="e.g. Suspected UTI, on metformin" value={notes} onChange={(e) => setNotes(e.target.value)} />}
      </Field>

      <Button type="submit" icon="flask" disabled={!selected.length} loading={order.busy}>
        {selected.length ? `Order ${selected.length} test${selected.length > 1 ? 's' : ''}` : 'Select tests to order'}
      </Button>
    </form>
  );
}
