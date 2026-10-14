import { useCallback, useEffect, useRef, useState } from 'react';
import { apiErrorMessage } from '../api/errors.js';

/**
 * Runs `loader` on mount and whenever `deps` change. `reload()` re-runs it; pass { silent: true }
 * to refresh in the background without flipping `loading` (used by polling).
 * Responses that arrive after a newer request started are ignored.
 */
export default function useAsync(loader, deps = [], { initial = null, enabled = true } = {}) {
  const [data, setData] = useState(initial);
  const [loading, setLoading] = useState(enabled);
  const [error, setError] = useState('');
  const requestId = useRef(0);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const run = useCallback(loader, deps);

  const reload = useCallback(async ({ silent = false } = {}) => {
    const id = ++requestId.current;
    if (!silent) setLoading(true);
    try {
      const result = await run();
      if (id === requestId.current) {
        setData(result);
        setError('');
      }
    } catch (err) {
      if (id === requestId.current) setError(apiErrorMessage(err, 'Could not load data. Try refreshing.'));
    } finally {
      if (id === requestId.current) setLoading(false);
    }
  }, [run]);

  useEffect(() => {
    if (enabled) reload();
    else {
      setData(initial);
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reload, enabled]);

  return { data, setData, loading, error, reload };
}
