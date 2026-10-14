// Labels, badge tones and display helpers for clinical data returned by the services.
// Status and flag names mirror the Java enums; keep them in step.

export const VISIT_STATUS = {
  REGISTERED: { label: 'Awaiting triage', tone: 'neutral' },
  TRIAGED: { label: 'Waiting for doctor', tone: 'info' },
  IN_CONSULTATION: { label: 'In consultation', tone: 'success' },
  AWAITING_LAB: { label: 'Awaiting lab', tone: 'warning' },
  LAB_RESULTS_READY: { label: 'Lab results ready', tone: 'info' },
  COMPLETED: { label: 'Completed', tone: 'neutral' },
  CANCELLED: { label: 'Cancelled', tone: 'neutral' },
};

export const PRIORITY = {
  EMERGENCY: { label: 'Emergency', tone: 'danger' },
  URGENT: { label: 'Urgent', tone: 'warning' },
  NORMAL: { label: 'Normal', tone: 'neutral' },
};

export const LAB_STATUS = {
  ORDERED: { label: 'Awaiting sample', tone: 'neutral' },
  SAMPLE_COLLECTED: { label: 'Sample collected', tone: 'info' },
  IN_PROGRESS: { label: 'In progress', tone: 'warning' },
  RELEASED: { label: 'Released', tone: 'success' },
  CANCELLED: { label: 'Cancelled', tone: 'neutral' },
};

export const LAB_PRIORITY = {
  STAT: { label: 'STAT', tone: 'danger' },
  URGENT: { label: 'Urgent', tone: 'warning' },
  ROUTINE: { label: 'Routine', tone: 'neutral' },
};

/** Result / vital flags. `code` is printed next to values, as on the PDF report. */
export const FLAG = {
  NORMAL: { label: 'Normal', code: '', level: 0 },
  NONE: { label: '', code: '', level: 0 },
  LOW: { label: 'Low', code: 'L', level: 1 },
  HIGH: { label: 'High', code: 'H', level: 1 },
  ABNORMAL: { label: 'Abnormal', code: 'A', level: 1 },
  CRITICAL_LOW: { label: 'Critical low', code: 'LL', level: 2 },
  CRITICAL_HIGH: { label: 'Critical high', code: 'HH', level: 2 },
};

export const flagLevel = (flag) => FLAG[flag]?.level ?? 0;
export const isCritical = (flag) => flagLevel(flag) === 2;
export const isAbnormal = (flag) => flagLevel(flag) >= 1;

export const meta = (map, key) => map[key] ?? { label: key ?? '–', tone: 'neutral' };

export const GENDERS = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' },
];
export const genderShort = (g) => ({ MALE: 'M', FEMALE: 'F', OTHER: 'O' }[g] ?? '–');
export const BLOOD_GROUPS = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];

export const VITALS = [
  { key: 'temperatureC', label: 'Temperature', unit: '°C', step: '0.1', min: 25, max: 45 },
  { key: 'pulseBpm', label: 'Pulse', unit: 'bpm', step: '1', min: 20, max: 250 },
  { key: 'respiratoryRate', label: 'Resp. rate', unit: '/min', step: '1', min: 4, max: 80 },
  { key: 'systolicBp', label: 'BP systolic', unit: 'mmHg', step: '1', min: 40, max: 300 },
  { key: 'diastolicBp', label: 'BP diastolic', unit: 'mmHg', step: '1', min: 20, max: 200 },
  { key: 'spo2', label: 'SpO₂', unit: '%', step: '1', min: 50, max: 100 },
  { key: 'weightKg', label: 'Weight', unit: 'kg', step: '0.1', min: 0.5, max: 400 },
  { key: 'heightCm', label: 'Height', unit: 'cm', step: '0.1', min: 30, max: 250 },
];

// Same adult limits as opd-service VitalsAssessor (low, high, criticalLow, criticalHigh). Preview only;
// the server's flags are what get stored and shown to the doctor.
const VITAL_LIMITS = {
  temperatureC: [36.1, 37.5, 35.0, 40.0],
  pulseBpm: [60, 100, 40, 140],
  respiratoryRate: [12, 20, 8, 30],
  systolicBp: [90, 139, 80, 180],
  diastolicBp: [60, 89, 40, 120],
  spo2: [95, null, 90, null],
};

export function previewVitalFlag(key, raw, ageYears) {
  const limits = VITAL_LIMITS[key];
  const v = raw === '' || raw == null ? null : Number(raw);
  if (!limits || v == null || Number.isNaN(v)) return null;
  return numericFlag(v, { low: limits[0], high: limits[1], criticalLow: limits[2], criticalHigh: limits[3] }, ageYears < 12);
}

/** Mirrors lab-service ResultFlagEvaluator so technicians see the flag while typing. */
export function previewResultFlag(result, raw) {
  const value = raw == null ? '' : String(raw).trim();
  if (value === '') return null;
  if (result.valueType === 'NARRATIVE') return 'NONE';
  if (result.valueType === 'TEXT') {
    if (!result.normalValues?.length) return 'NONE';
    return result.normalValues.some((n) => n.toLowerCase() === value.toLowerCase()) ? 'NORMAL' : 'ABNORMAL';
  }
  const v = Number(value);
  if (Number.isNaN(v)) return 'INVALID';
  return numericFlag(v, {
    low: result.refLow, high: result.refHigh, criticalLow: result.criticalLow, criticalHigh: result.criticalHigh,
  });
}

function numericFlag(v, { low, high, criticalLow, criticalHigh }, criticalOnly = false) {
  const has = (x) => x !== null && x !== undefined;
  if (!has(low) && !has(high) && !has(criticalLow) && !has(criticalHigh)) return 'NONE';
  if (has(criticalLow) && v < criticalLow) return 'CRITICAL_LOW';
  if (has(criticalHigh) && v > criticalHigh) return 'CRITICAL_HIGH';
  if (!criticalOnly && has(low) && v < low) return 'LOW';
  if (!criticalOnly && has(high) && v > high) return 'HIGH';
  return 'NORMAL';
}

// ---------------------------------------------------------------- time

export function formatTime(value) {
  if (!value) return '–';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '–';
  return d.toDateString() === new Date().toDateString()
    ? d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    : d.toLocaleString([], { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
}

export function formatDate(value) {
  if (!value) return '–';
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? '–' : d.toLocaleDateString([], { day: '2-digit', month: 'short', year: 'numeric' });
}

/** "12 min", "1 h 05 min" since the timestamp. */
export function elapsedSince(value, now = Date.now()) {
  if (!value) return null;
  const minutes = Math.floor((now - new Date(value).getTime()) / 60000);
  if (Number.isNaN(minutes) || minutes < 0) return null;
  if (minutes < 1) return 'just now';
  if (minutes < 60) return `${minutes} min`;
  return `${Math.floor(minutes / 60)} h ${String(minutes % 60).padStart(2, '0')} min`;
}

export const ageSex = (p) => (p ? `${p.ageDisplay} · ${genderShort(p.gender)}` : '');
