/**
 * Maps an axios error to a message for the user. The services return
 * { status, message, fieldErrors } bodies written for end users, so 4xx messages are shown as-is.
 */
export function apiErrorMessage(err, fallback = 'Something went wrong. Please try again.') {
  if (!err?.response) return 'Cannot reach the server. Check your connection and try again.';
  const { status, data } = err.response;
  if (status === 401) return 'Your session has expired. Please sign in again.';
  if (status === 403) return 'Your role is not allowed to do this.';
  if (status >= 500 && status !== 502) return fallback;
  const fields = data?.fieldErrors && Object.entries(data.fieldErrors);
  if (fields?.length) return fields.map(([field, msg]) => `${humanise(field)}: ${msg}`).join(' · ');
  return typeof data?.message === 'string' ? data.message : fallback;
}

const humanise = (field) => field.replace(/([A-Z])/g, ' $1').replace(/^./, (c) => c.toUpperCase());
