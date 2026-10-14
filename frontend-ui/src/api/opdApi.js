import axiosClient, { OPD_API, data } from './axiosClient.js';

// One function per opd-service endpoint. Components never build URLs.
export const patientApi = {
  search: (q) => data(axiosClient.get(`${OPD_API}/patients/search`, { params: { q } })),
  get: (id) => data(axiosClient.get(`${OPD_API}/patients/${id}`)),
  register: (patient) => data(axiosClient.post(`${OPD_API}/patients`, patient)),
  update: (id, patient) => data(axiosClient.put(`${OPD_API}/patients/${id}`, patient)),
  history: (id) => data(axiosClient.get(`${OPD_API}/patients/${id}/visits`)),
};

// Repeated params (?status=A&status=B) are what Spring binds to a Set.
const statusParams = (statuses) => {
  const params = new URLSearchParams();
  (statuses ?? []).forEach((s) => params.append('status', s));
  return params;
};

export const visitApi = {
  today: (statuses) => data(axiosClient.get(`${OPD_API}/visits`, { params: statusParams(statuses) })),
  doctorQueue: () => data(axiosClient.get(`${OPD_API}/visits/doctor-queue`)),
  get: (id) => data(axiosClient.get(`${OPD_API}/visits/${id}`)),
  open: (visit) => data(axiosClient.post(`${OPD_API}/visits`, visit)),
  recordVitals: (id, vitals) => data(axiosClient.post(`${OPD_API}/visits/${id}/vitals`, vitals)),
  cancel: (id, reason) => data(axiosClient.post(`${OPD_API}/visits/${id}/cancel`, { reason })),
  startConsultation: (id) => data(axiosClient.post(`${OPD_API}/visits/${id}/start-consultation`)),
  saveConsultation: (id, notes) => data(axiosClient.put(`${OPD_API}/visits/${id}/consultation`, notes)),
  orderLab: (id, order) => data(axiosClient.post(`${OPD_API}/visits/${id}/lab-orders`, order)),
  complete: (id) => data(axiosClient.post(`${OPD_API}/visits/${id}/complete`)),
};
