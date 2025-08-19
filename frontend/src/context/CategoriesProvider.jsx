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

  const lastLoadedAtRef = useRef(0);
  const categoriesCountRef = useRef(0);

  // keep a single in-flight request
  const ctrlRef = useRef(null);
  const inFlightRef = useRef(false);
  const mountedRef = useRef(false);
  const prevTokenRef = useRef(null);

  const fetchOnce = useCallback(
    async ({ force = false } = {}) => {
      if (inFlightRef.current) return;
      
      const now = Date.now();
      const fresh = now - lastLoadedAtRef.current < staleMs;
      const hasData = categoriesCountRef.current > 0;
      if (!force && fresh && hasData) return;

      ctrlRef.current?.abort();
      const controller = new AbortController();
      ctrlRef.current = controller;

      inFlightRef.current = true;
      setLoading(true);
      setError("");

      try {
        const res = await api.get("/categories", controller ? { signal: controller.signal } : undefined);
        if (!mountedRef.current) return;
        const list = Array.isArray(res.data) ? res.data : [];
        setCategories(list);
        const ts = Date.now();
        setLastLoadedAt(ts);
        lastLoadedAtRef.current = ts;
        categoriesCountRef.current = list.length;
      } catch (err) {
        if (controller.signal.aborted) return;
        console.error("Fetch /categories failed:", err?.response?.status, err);
        const msg =
          err?.response?.data?.message ||
          (err?.code === "ERR_NETWORK" ? "Network error. Check your connection." : "Error fetching categories");
        if (mountedRef.current) {
          setError(msg);
          if (err?.response?.status === 401 || err?.response?.status === 403) {
            setCategories([]);
            setLastLoadedAt(0);
            categoriesCountRef.current = 0;
            lastLoadedAtRef.current = 0;
          }
        } 
      } finally {
        if (mountedRef.current) setLoading(false);
        inFlightRef.current = false;
      }
    },
    [staleMs]
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
    // Logged out: clear everything
    if (!token) {
      setCategories([]);
      setLastLoadedAt(0);
      setError("");
      categoriesCountRef.current = 0;
      lastLoadedAtRef.current = 0;
      prevTokenRef.current = null;
      return;
    }
    // Logged in: detect user/session change by token swap
    if (prevTokenRef.current !== token) {
      // Abort old request and clear cache so UI doesn't show prior user's data
      ctrlRef.current?.abort();
      setCategories([]);
      setLastLoadedAt(0);
      categoriesCountRef.current = 0;
      lastLoadedAtRef.current = 0;
      prevTokenRef.current = token;
      // Force a fresh fetch for the new user
      fetchOnce({ force: true });
      return;
    }
    // Same user/session: honor staleness policyF
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
