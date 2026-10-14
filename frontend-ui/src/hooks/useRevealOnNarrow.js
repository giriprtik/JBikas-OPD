import { useEffect, useRef } from 'react';

/**
 * On stacked (narrow) layouts the workspace renders below the list, so tapping a row would
 * otherwise appear to do nothing. Attach the returned ref to the workspace root.
 */
export default function useRevealOnNarrow(ready = true) {
  const ref = useRef(null);
  useEffect(() => {
    if (ready && ref.current && window.matchMedia('(max-width: 1100px)').matches) {
      ref.current.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }, [ready]);
  return ref;
}
