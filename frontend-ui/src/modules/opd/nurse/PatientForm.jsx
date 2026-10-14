import { useState } from 'react';
import { Alert, Button, Field } from '../../../components/ui/index.jsx';
import { BLOOD_GROUPS, GENDERS } from '../../../domain/clinical.js';
import { patientApi } from '../../../api/opdApi.js';
import useAction from '../../../hooks/useAction.js';

const EMPTY = {
  fullName: '', dateOfBirth: '', ageYears: '', gender: 'MALE', phone: '', address: '',
  bloodGroup: '', allergies: '', emergencyContactName: '', emergencyContactPhone: '',
};

const blank = (v) => (v === '' || v == null ? null : v);

/** New patient registration. Either date of birth or age is required. */
export default function PatientForm({ initialName = '', onSaved, onCancel }) {
  const [form, setForm] = useState({ ...EMPTY, fullName: initialName });
  const [ageMode, setAgeMode] = useState('age');
  const save = useAction();

  const bind = (key) => ({
    value: form[key],
    onChange: (e) => setForm((f) => ({ ...f, [key]: e.target.value })),
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = Object.fromEntries(Object.entries(form).map(([k, v]) => [k, blank(typeof v === 'string' ? v.trim() : v)]));
    if (ageMode === 'age') payload.dateOfBirth = null;
    else payload.ageYears = null;
    if (payload.ageYears != null) payload.ageYears = Number(payload.ageYears);
    const saved = await save.run(() => patientApi.register(payload), 'Could not register the patient.');
    if (saved) onSaved(saved);
  };

  return (
    <form className="form-grid" onSubmit={handleSubmit}>
      {save.error && <Alert tone="error" onClose={() => save.setError('')}>{save.error}</Alert>}

      <Field label="Full name">
        {(id) => <input id={id} className="input" required autoFocus autoComplete="off" {...bind('fullName')} />}
      </Field>

      <div className="form-grid form-grid--2">
        <div className="field">
          <div className="segmented" role="radiogroup" aria-label="Age or date of birth">
            {[['age', 'Age'], ['dob', 'Date of birth']].map(([v, label]) => (
              <label key={v} className="segmented__option">
                <input type="radio" name="ageMode" value={v} checked={ageMode === v} onChange={() => setAgeMode(v)} />
                {label}
              </label>
            ))}
          </div>
          {ageMode === 'age' ? (
            <input className="input" aria-label="Age in years" type="number" inputMode="numeric" min="0" max="130"
              placeholder="Years" required {...bind('ageYears')} />
          ) : (
            <input className="input" aria-label="Date of birth" type="date" required
              max={new Date().toISOString().slice(0, 10)} {...bind('dateOfBirth')} />
          )}
        </div>
        <Field label="Sex">
          {(id) => (
            <select id={id} className="input" {...bind('gender')}>
              {GENDERS.map((g) => <option key={g.value} value={g.value}>{g.label}</option>)}
            </select>
          )}
        </Field>
      </div>

      <div className="form-grid form-grid--2">
        <Field label="Phone">
          {(id) => <input id={id} className="input" type="tel" inputMode="tel" placeholder="98XXXXXXXX" required {...bind('phone')} />}
        </Field>
        <Field label="Blood group">
          {(id) => (
            <select id={id} className="input" {...bind('bloodGroup')}>
              <option value="">Unknown</option>
              {BLOOD_GROUPS.map((g) => <option key={g}>{g}</option>)}
            </select>
          )}
        </Field>
      </div>

      <Field label="Address">
        {(id) => <input id={id} className="input" autoComplete="off" {...bind('address')} />}
      </Field>

      <Field label="Known allergies" hint="Drugs, food or other. Shown in red on every clinical screen.">
        {(id) => <input id={id} className="input" placeholder="e.g. Penicillin" {...bind('allergies')} />}
      </Field>

      <div className="form-grid form-grid--2">
        <Field label="Emergency contact">
          {(id) => <input id={id} className="input" {...bind('emergencyContactName')} />}
        </Field>
        <Field label="Emergency contact phone">
          {(id) => <input id={id} className="input" type="tel" inputMode="tel" {...bind('emergencyContactPhone')} />}
        </Field>
      </div>

      <div className="button-row">
        {onCancel && <Button type="button" variant="secondary" onClick={onCancel}>Back to search</Button>}
        <Button type="submit" icon="check" loading={save.busy}>Register patient</Button>
      </div>
    </form>
  );
}
