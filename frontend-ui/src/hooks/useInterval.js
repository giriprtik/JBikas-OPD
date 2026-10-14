import { useEffect, useRef } from 'react';

/** Calls `callback` every `ms` while the tab is visible. */
export default function useInterval(callback, ms) {
  const saved = useRef(callback);
  saved.current = callback;

  useEffect(() => {
    if (!ms) return undefined;
    const id = setInterval(() => {
      if (document.visibilityState === 'visible') saved.current();
    }, ms);
    return () => clearInterval(id);
  }, [ms]);
}
