import axiosClient, { LAB_API, data } from './axiosClient.js';

const statusParams = (statuses) => {
  const params = new URLSearchParams();
  (statuses ?? []).forEach((s) => params.append('status', s));
  return params;
};

export const labApi = {
  catalog: () => data(axiosClient.get(`${LAB_API}/catalog`)),
  worklist: (statuses) => data(axiosClient.get(`${LAB_API}/orders`, { params: statusParams(statuses) })),
  get: (id) => data(axiosClient.get(`${LAB_API}/orders/${id}`)),
  byVisit: (visitId) => data(axiosClient.get(`${LAB_API}/orders/by-visit/${visitId}`)),
  byPatient: (patientId) => data(axiosClient.get(`${LAB_API}/orders/by-patient/${patientId}`)),
  collectSample: (id) => data(axiosClient.post(`${LAB_API}/orders/${id}/collect-sample`)),
  saveResults: (id, results) => data(axiosClient.put(`${LAB_API}/orders/${id}/results`, { results })),
  release: (id, criticalNotifiedTo) =>
    data(axiosClient.post(`${LAB_API}/orders/${id}/release`, { criticalNotifiedTo: criticalNotifiedTo || null })),
  amend: (id, resultId, value, reason) =>
    data(axiosClient.post(`${LAB_API}/orders/${id}/results/${resultId}/amend`, { value, reason })),
  cancel: (id, reason) => data(axiosClient.post(`${LAB_API}/orders/${id}/cancel`, { reason })),

  uploadAttachment: (id, file, description) => {
    const form = new FormData();
    form.append('file', file);
    if (description) form.append('description', description);
    return data(axiosClient.post(`${LAB_API}/orders/${id}/attachments`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }));
  },
  deleteAttachment: (id, attachmentId) => axiosClient.delete(`${LAB_API}/orders/${id}/attachments/${attachmentId}`),

  /** Opens the PDF report (or an attachment) in a new tab. Fetched with the token, then shown as a blob. */
  openReport: (id) => openBlob(`${LAB_API}/orders/${id}/report`),
  openAttachment: (id, attachmentId) => openBlob(`${LAB_API}/orders/${id}/attachments/${attachmentId}`),
};

async function openBlob(url) {
  // Open the tab synchronously so popup blockers allow it, then point it at the blob.
  const tab = window.open('', '_blank');
  try {
    const res = await axiosClient.get(url, { responseType: 'blob' });
    const blobUrl = URL.createObjectURL(res.data);
    if (tab) tab.location.href = blobUrl;
    else window.location.assign(blobUrl);
    setTimeout(() => URL.revokeObjectURL(blobUrl), 60_000);
  } catch (err) {
    tab?.close();
    // Error bodies arrive as blobs too; turn them back into JSON so apiErrorMessage can read them.
    if (err?.response?.data instanceof Blob) {
      try {
        err.response.data = JSON.parse(await err.response.data.text());
      } catch {
        /* keep original */
      }
    }
    throw err;
  }
}
