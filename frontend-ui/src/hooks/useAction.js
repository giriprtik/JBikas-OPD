import { useState } from 'react';
import { apiErrorMessage } from '../api/errors.js';

/**
 * Wraps a mutating call with busy and error state.
 *   const save = useAction();
 *   await save.run(() => api.update(...));   // returns the result, or undefined on failure
 */
export default function useAction() {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const run = async (fn, fallback) => {
    setBusy(true);
    setError('');
    try {
      return await fn();
    } catch (err) {
      setError(apiErrorMessage(err, fallback));
      return undefined;
    } finally {
      setBusy(false);
    }
  };

  return { busy, error, setError, run };
}
