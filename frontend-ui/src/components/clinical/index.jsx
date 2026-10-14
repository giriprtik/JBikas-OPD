// Clinical building blocks shared by the nurse, doctor and lab screens.
import { Badge, Icon } from '../ui/index.jsx';
import {
  FLAG, VITALS, ageSex, formatDate, isAbnormal, isCritical, meta,
} from '../../domain/clinical.js';

export function StatusBadge({ map, value }) {
  const m = meta(map, value);
  return <Badge tone={m.tone}>{m.label}</Badge>;
}

/** "H", "LL" etc. next to a value. Renders nothing for normal values. */
export function FlagMark({ flag }) {
  const f = FLAG[flag];
  if (!f || !f.code) return null;
  const critical = f.level === 2;
  const arrow = flag.endsWith('HIGH') ? '↑' : flag.endsWith('LOW') ? '↓' : '';
  return (
    <span className={`flag ${critical ? 'flag--critical' : 'flag--abnormal'}`} title={f.label}>
      {arrow}{f.code}
      <span className="sr-only"> ({f.label})</span>
    </span>
  );
}

/** Value text coloured by flag: red for abnormal, red badge for critical. */
export function FlaggedValue({ value, unit, flag }) {
  return (
    <span className={isAbnormal(flag) ? 'value value--abnormal' : 'value'}>
      {value ?? '–'}
      {unit && value != null && <span className="value__unit"> {unit}</span>}
      {' '}
      <FlagMark flag={flag} />
    </span>
  );
}

/** Identity strip at the top of every clinical workspace. Allergies are always shown when recorded. */
export function PatientBanner({ patient, children }) {
  if (!patient) return null;
  return (
    <div className="patient-banner">
      <div className="patient-banner__main">
        <span className="avatar avatar--light" aria-hidden="true"><Icon name="user" size={18} /></span>
        <div>
          <div className="patient-banner__name">{patient.fullName}</div>
          <div className="patient-banner__meta">
            <span className="mono">{patient.mrn}</span>
            <span>{ageSex(patient)}{patient.dobEstimated ? ' (est.)' : ''}</span>
            {patient.bloodGroup && <span>Blood {patient.bloodGroup}</span>}
            {patient.phone && <span>{patient.phone}</span>}
          </div>
        </div>
        {children && <div className="patient-banner__actions">{children}</div>}
      </div>
      {patient.allergies && (
        <div className="allergy" role="note">
          <Icon name="alert" size={15} />
          <strong>Allergies:</strong> {patient.allergies}
        </div>
      )}
    </div>
  );
}

/** Latest vitals as tiles; abnormal ones in red, critical ones on a red background. */
export function VitalsSummary({ vitals, compact }) {
  if (!vitals) return <p className="muted">No vitals recorded yet.</p>;
  const tiles = [
    ...VITALS.filter((v) => !['systolicBp', 'diastolicBp'].includes(v.key)).map((v) => ({
      key: v.key, label: v.label, unit: v.unit, value: vitals[v.key], flag: vitals.flags?.[v.key],
    })),
  ];
  if (vitals.systolicBp != null) {
    const flags = [vitals.flags?.systolicBp, vitals.flags?.diastolicBp];
    const worst = flags.find(isCritical) ?? flags.find(isAbnormal) ?? 'NORMAL';
    tiles.splice(2, 0, { key: 'bp', label: 'Blood pressure', unit: 'mmHg', value: `${vitals.systolicBp}/${vitals.diastolicBp}`, flag: worst });
  }
  if (vitals.bmi != null) tiles.push({ key: 'bmi', label: 'BMI', unit: 'kg/m²', value: vitals.bmi });
  const shown = tiles.filter((t) => t.value != null);
  return (
    <div className={compact ? 'vitals vitals--compact' : 'vitals'}>
      {shown.map((t) => (
        <div key={t.key} className={`vital ${isCritical(t.flag) ? 'vital--critical' : isAbnormal(t.flag) ? 'vital--abnormal' : ''}`}>
          <div className="vital__label">{t.label}</div>
          <div className="vital__value">
            {t.value}<span className="value__unit"> {t.unit}</span> <FlagMark flag={t.flag} />
          </div>
        </div>
      ))}
    </div>
  );
}

/**
 * Read-only results of one lab order, grouped by test. Abnormal rows are red, critical rows shaded.
 * `renderAction(result)` adds a trailing cell (used for "Amend" in the lab).
 */
export function LabResultsTable({ order, renderAction }) {
  return (
    <div className="results">
      {order.items.map((item) => (
        <div key={item.id} className="results__group">
          <div className="results__test">
            {item.testName}
            {item.specimen && <span className="muted"> · {item.specimen}</span>}
          </div>
          <div className="table-wrap">
            <table className="table table--results">
              <thead>
                <tr>
                  <th scope="col">Investigation</th>
                  <th scope="col">Result</th>
                  <th scope="col" className="col-hide-sm">Reference</th>
                  {renderAction && <th scope="col"><span className="sr-only">Actions</span></th>}
                </tr>
              </thead>
              <tbody>
                {item.results.map((r) => (
                  r.valueType === 'NARRATIVE' ? (
                    r.value ? (
                      <tr key={r.id}>
                        <td className="cell-primary">{r.parameterName}{r.amendments?.length ? ' *' : ''}</td>
                        <td colSpan={2} className="narrative">{r.value}</td>
                        {renderAction && <td>{renderAction(r)}</td>}
                      </tr>
                    ) : null
                  ) : (
                    <tr key={r.id} className={isCritical(r.flag) ? 'row--critical' : isAbnormal(r.flag) ? 'row--abnormal' : ''}>
                      <td>{r.parameterName}{r.amendments?.length ? ' *' : ''}</td>
                      <td><FlaggedValue value={r.value} unit={r.unit} flag={r.flag} /></td>
                      <td className="cell-secondary col-hide-sm">{r.referenceText ?? '–'}</td>
                      {renderAction && <td className="cell-action">{renderAction(r)}</td>}
                    </tr>
                  )
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}
      {order.items.some((i) => i.results.some((r) => r.amendments?.length)) && (
        <div className="amendments">
          <div className="field__label">* Amended</div>
          {order.items.flatMap((i) => i.results).filter((r) => r.amendments?.length).flatMap((r) => r.amendments.map((a, n) => (
            <p key={`${r.id}-${n}`} className="cell-secondary">
              {r.parameterName}: {a.previousValue ?? '–'} → {a.newValue ?? '–'} ({a.reason}) by {a.amendedBy}, {formatDate(a.amendedAt)}
            </p>
          )))}
        </div>
      )}
    </div>
  );
}
