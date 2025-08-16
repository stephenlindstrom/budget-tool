import { useEffect, useMemo, useRef, useState, useCallback } from "react";
import api from "../api/api";
import { useAuth } from "../hooks/useAuth";
import { CategoriesContext } from "./CategoriesContext";

export function CategoriesProvider({ children, staleMs = 5 * 60_000 }) {
  const { token, loading: authLoading } = useAuth();
  const [ categories, setCategories ] = useState([]);
  const [ loading, setLoading ] = useState(false);
  const [ error, setError ] = useState("");
  const [ lastLoadedAt, setLastLoadedAt ] = useState(0);

  // keep a single in-flight request
  const ctrlRef = useRef(null);
  const inFlightRef = useRef(false);
  const mountedRef = useRef(false);

  const fetchOnce = useCallback(
    async ({ force = false } = {}) => {
      if (inFlightRef.current) return;
      if (!force && Date.now() - lastLoadedAt < staleMs && categories.length) return;

      ctrlRef.current?.abort();
      const controller = new AbortController();
      ctrlRef.current = controller;

      inFlightRef.current = true;
      setLoading(true);
      setError("");

      try {
        const res = await api.get("/categories", { signal: controller.signal });
        if (!mountedRef.current) return;
        setCategories(Array.isArray(res.data) ? res.data : []);
        setLastLoadedAt(Date.now());
      } catch (err) {
        if (controller.signal.aborted) return;
        console.error("Fetch /categories failed:", err?.response?.status, err);
        const msg =
          err?.response?.data?.message ||
          (err?.code === "ERR_NETWORK" ? "Network error. Check your connection." : "Error fetching categories");
        if (mountedRef.current) setError(msg);
      } finally {
        if (mountedRef.current) setLoading(false);
        inFlightRef.current = false;
      }
    },
    [categories.length, lastLoadedAt, staleMs]
  );

  // public refresh() to re-load on demand
  const refresh = useCallback(() => fetchOnce({ force: true }), [fetchOnce]);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
      ctrlRef.current?.abort();
    }
  }, []);

  useEffect(() => {
    if (authLoading) return;
    if (!token) {
      setCategories([]);
      setLastLoadedAt(0);
      setError("");
      return;
    }
    fetchOnce({ force: false });
  }, [authLoading, token, fetchOnce]);

  const byId = useMemo(() => {
    const m = new Map();
    for (const c of categories) m.set(String(c.id), c);
    return m;
  }, [categories]);

  const value = useMemo(
    () => ({
      categories,
      byId,
      loading,
      error,
      refresh,
      lastLoadedAt,
    }),
    [categories, byId, loading, error, refresh, lastLoadedAt]
  );

  return (
    <CategoriesContext.Provider value={value}>
      {children}
    </CategoriesContext.Provider>
  );
}
