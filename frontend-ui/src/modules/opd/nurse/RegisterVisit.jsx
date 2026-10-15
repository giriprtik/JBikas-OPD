import { useEffect, useState } from 'react';
import { Alert, Button, Card, EmptyState, Field, Icon } from '../../../components/ui/index.jsx';
import { PatientBanner } from '../../../components/clinical/index.jsx';
import { PRIORITY, ageSex } from '../../../domain/clinical.js';
import { patientApi, visitApi } from '../../../api/opdApi.js';
import { apiErrorMessage } from '../../../api/errors.js';
import useAction from '../../../hooks/useAction.js';
import PatientForm from './PatientForm.jsx';

/**
 * Front desk: find the patient (or register them), then open today's visit and issue a token.
 * Searching first avoids duplicate records for returning patients.
 */
export default function RegisterVisit({ onVisitOpened }) {
  const [patient, setPatient] = useState(null);
  const [mode, setMode] = useState('search');
  const [query, setQuery] = useState('');

  const reset = () => {
    setPatient(null);
    setMode('search');
    setQuery('');
  };

  return (
    <div className="split split--form-left">
      <Card
        title={mode === 'new' ? 'New patient' : 'Find patient'}
        subtitle={mode === 'new' ? 'Register a first-time patient.' : 'Search by MRN, phone number or name.'}
      >
        {mode === 'new' ? (
          <PatientForm
            initialName={/\d/.test(query) ? '' : query}
            onCancel={() => setMode('search')}
            onSaved={(p) => { setPatient(p); setMode('search'); }}
          />
        ) : (
          <PatientSearch query={query} setQuery={setQuery} selectedId={patient?.id} onSelect={setPatient} onNew={() => setMode('new')} />
        )}
      </Card>

      {patient ? (
        <OpenVisitForm
          key={patient.id}
          patient={patient}
          onCancel={reset}
          onOpened={(visit) => { reset(); onVisitOpened(visit); }}
        />
      ) : (
        <Card title="Open visit">
          <EmptyState icon="user" title="No patient selected">
            Search for the patient first. If they have not been here before, register them as a new patient.
          </EmptyState>
        </Card>
      )}
    </div>
  );
}

function PatientSearch({ query, setQuery, selectedId, onSelect, onNew }) {
  const [results, setResults] = useState(null);
  const [error, setError] = useState('');
  const [searching, setSearching] = useState(false);

  // Debounced search as the nurse types.
  useEffect(() => {
    const q = query.trim();
    if (q.length < 2) {
      setResults(null);
      return undefined;
    }
    let cancelled = false;
    const timer = setTimeout(async () => {
      setSearching(true);
      try {
        const found = await patientApi.search(q);
        if (!cancelled) { setResults(found); setError(''); }
      } catch (err) {
        if (!cancelled) setError(apiErrorMessage(err, 'Search failed.'));
      } finally {
        if (!cancelled) setSearching(false);
      }
    }, 300);
    return () => { cancelled = true; clearTimeout(timer); };
  }, [query]);

  return (
    <div className="form-grid">
      <div className="search-box">
        <Icon name="search" size={16} />
        <label className="sr-only" htmlFor="patient-lookup">Search patients</label>
        <input
          id="patient-lookup"
          className="input"
          type="search"
          autoFocus
          placeholder="JB-000123, 98XXXXXXXX or name"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        {searching && <span className="spinner" aria-hidden="true" />}
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      {results && (
        results.length === 0 ? (
          <p className="muted">No patient found for “{query.trim()}”.</p>
        ) : (
          <ul className="pick-list" aria-label="Matching patients">
            {results.map((p) => (
              <li key={p.id}>
                <button type="button" className="pick-list__item" aria-pressed={p.id === selectedId} onClick={() => onSelect(p)}>
                  <span>
                    <span className="cell-primary">{p.fullName}</span>
                    <span className="cell-secondary"> · {ageSex(p)} · {p.phone}</span>
                  </span>
                  <span className="mono cell-secondary">{p.mrn}</span>
                </button>
              </li>
            ))}
          </ul>
        )
      )}

      <Button type="button" variant="secondary" icon="plus" onClick={onNew}>New patient</Button>
    </div>
  );
}

function OpenVisitForm({ patient, onCancel, onOpened }) {
  const [complaint, setComplaint] = useState('');
  const [priority, setPriority] = useState('NORMAL');
  const [existingVisit, setExistingVisit] = useState(null);
  const open = useAction();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setExistingVisit(null);
    
    const visit = await open.run(
      async () => {
        try {
          return await visitApi.open({ patientId: patient.id, chiefComplaint: complaint.trim(), priority });
        } catch (err) {
          // Detect microservice conflict response patterns
          if (err.response && (err.response.status === 409 || err.response.status === 422)) {
            if (err.response.data?.existingVisit) {
              setExistingVisit(err.response.data.existingVisit);
            }
          }
          throw err;
        }
      },
      'Could not open the visit.',
    );
    if (visit) onOpened(visit);
  };

  return (
    <Card title="Open visit" subtitle="Issues today's OPD token.">
      <form className="form-grid" onSubmit={handleSubmit}>
        <PatientBanner patient={patient}>
          <Button type="button" size="sm" variant="secondary" onClick={onCancel}>Change</Button>
        </PatientBanner>

        {open.error && (
          <div className="form-grid" style={{ gridColumn: '1 / -1' }}>
            <Alert tone="error" onClose={() => open.setError('')}>
              <div className="flex flex-col gap-1">
                <span>{open.error}</span>
                {existingVisit ? (
                  <div className="mt-2 p-2 bg-white rounded border border-red-200 flex items-center justify-between" style={{ color: '#333' }}>
                    <span className="text-xs">Patient already possesses active Token <strong>#{existingVisit.tokenNumber}</strong>.</span>
                    <Button 
                      type="button" 
                      size="sm" 
                      onClick={() => onOpened(existingVisit)}
                      style={{ marginLeft: '10px', padding: '4px 8px', fontSize: '11px' }}
                    >
                      Recover Active Token Workspace
                    </Button>
                  </div>
                ) : (
                  <span className="text-xs block mt-1 opacity-80">
                    If this patient is new, check database daily_token_counters sync settings.
                  </span>
                )}
              </div>
            </Alert>
          </div>
        )}

        <Field label="Chief complaint" hint="In the patient's words, with duration.">
          {(id) => (
            <textarea id={id} className="input" required autoFocus value={complaint}
              placeholder="e.g. Fever and dry cough for 3 days"
              onChange={(e) => setComplaint(e.target.value)} />
          )}
        </Field>

        <fieldset className="fieldset-reset">
          <legend className="field__label section-label">Arrival priority</legend>
          <div className="segmented">
            {Object.entries(PRIORITY).map(([key, m]) => (
              <label key={key} className={`segmented__option segmented__option--${m.tone}`}>
                <input type="radio" name="priority" value={key} checked={priority === key} onChange={() => setPriority(key)} />
                {m.label}
              </label>
            ))}
          </div>
          <p className="field__hint">Emergency patients go straight to the top of the doctor's queue, even before vitals.</p>
        </fieldset>

        <Button type="submit" icon="check" loading={open.busy}>Open visit &amp; issue token</Button>
      </form>
    </Card>
  );
}
