import { useEffect, useState, useRef, useCallback, useMemo } from "react";
import { useAuth } from "../hooks/useAuth";
import api from "../api/api";
import { ChevronUp, ChevronDown, ChevronsUpDown } from "lucide-react";
import { useCategories } from "../hooks/useCategories";

function TransactionPage() {
  const { loading: authLoading } = useAuth();

  // ---- Categories (from context) ----
  const {
    categories,
    byId,
    loading: loadingCats,
    error: catsError,
    refresh: refreshCats
  } = useCategories();

  // ---- Transactions state ----
  const [transactions, setTransactions] = useState([]);
  const [transError, setTransError] = useState("");
  const [loadingTrans, setLoadingTrans] = useState(false);

  // ---- Filter state ----
  const [filter, setFilter] = useState({
    type: "",
    categoryId: "",
    startDate: "",
    endDate: "",
  });

  const onFilterChange = (e) =>
    setFilter((f) => ({...f, [e.target.name] : e.target.value }));

  const clearFilter = () =>
    setFilter({ type: "", categoryId: "", startDate: "", endDate: ""});

  // ---- Sorting ----
  const [sort, setSort] = useState({ key: "date", dir: "desc" });

  const dateFormatter = new Intl.DateTimeFormat("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });

  const currencyFormatter = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2,
  })

  // ---- Single in-flight controller for transactions ----
  const transCtrlRef = useRef(null);

  const fetchAll = useCallback(async () => {
    transCtrlRef.current?.abort();
    const controller = new AbortController();
    transCtrlRef.current = controller;

    setLoadingTrans(true);
    setTransError("");

    try {
        const res = await api.get("/transactions", { signal: controller.signal });
        setTransactions(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        if (controller.signal.aborted) return;
        console.error("Fetch /transactions failed:", err?.response?.status, err);
        const msg =
          err?.response?.data?.message || (err?.code === "ERR_NETWORK" 
            ? "Network error. Check your connection." 
            : "Error fetching transactions");
        setTransError(msg);
      } finally {
        if (!controller.signal.aborted) setLoadingTrans(false);
      }
  }, []);

  const fetchFiltered = useCallback(async () => {
    transCtrlRef.current?.abort();
    const controller = new AbortController();
    transCtrlRef.current = controller;

    setLoadingTrans(true);
    setTransError("");

    // only send non-empty params
    const params = {};
    if (filter.type) params.type = filter.type;
    if (filter.categoryId) params.categoryId = filter.categoryId;
    if (filter.startDate) params.startDate = filter.startDate;
    if (filter.endDate) params.endDate = filter.endDate;

    try {
      const res = await api.get("/transactions/filter", {
        params,
        signal: controller.signal,
      });
      setTransactions(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      if (controller.signal.aborted) return;
      console.error("Filter /transactions failed:", err?.response?.status, err);
      const msg =
        err?.response?.data?.message ||
        (err?.code === "ERR_NETWORK"
          ? "Network error. Check your connection."
          : "Error filtering transactions");
      setTransError(msg);
    } finally {
      if (!controller.signal.aborted) setLoadingTrans(false);
    }
  }, [filter]);

  // initial load
  useEffect(() => {
    if (!authLoading) fetchAll();
    return () => transCtrlRef.current?.abort();
  }, [authLoading, fetchAll]);

  const rows = useMemo(() => {
    const mult = sort.dir === "asc" ? 1 : -1;
    const cmp = (a, b) => {
      const val = sort.key === "date"
        ? Date.parse(`${a.date}T00:00:00`) - Date.parse(`${b.date}T00:00:00`)
        : sort.key === "amount"
        ? (Number(a.amount)||0) - (Number(b.amount)||0)
        : sort.key === "category"
        ? (byId.get(String(a.categoryId))?.name || "").localeCompare(
          byId.get(String(b.categoryId))?.name || "",
          undefined,
          { sensitivity: "base" }
        )
        : (a.description||"").localeCompare(b.description||"", undefined, { sensitivity: "base" });
      
      return val * mult;
    };
    return [...transactions].sort(cmp);
  }, [transactions, sort, byId]);

  const columns = [
    { id: "date", header: "Date", sortable: true, defaultDir: "desc" },
    { id: "description", header: "Description", sortable: true, defaultDir: "asc"},
    { id: "category", header: "Category", sortable: true, defaultDir: "asc"},
    { id: "amount", header: "Amount", align: "right", sortable: true, defaultDir: "desc" },
  ];

  const colCount = columns.length;
  const amountAlign = columns.find(c => c.id === "amount")?.align === "right" ? "text-right" : "";

  const view = transError ? "error" : loadingTrans ? "loading" : transactions.length === 0 ? "empty" : "data";

  const ariaSortFor = (id) =>
    sort.key === id ? (sort.dir === "asc" ? "ascending" : "descending") : "none";

  const onSortClick = (col) =>
    setSort((s) =>
      s.key === col.id
        ? { key: col.id, dir: s.dir === "asc" ? "desc" : "asc" }
        : { key: col.id, dir: col.defaultDir ?? "asc" }
    );
  
  return (
    <div className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8 mt-16 mb-24">
      <h2 className="text-2xl font-semibold tracking-tight text-slate-900 mb-4">
        Transactions
      </h2>

      {/* Filter Form */}
      <form
        className="grid gap-3 md:grid-cols-5 items-end mb-6"
        onSubmit={(e) => {
          e.preventDefault();
          const any = filter.type || filter.categoryId || filter.startDate || filter.endDate;
          any ? fetchFiltered() : fetchAll();
        }}
      >
        {/* Type */}
        <label className="flex flex-col">
          <span className="mb-1 text-sm font-medium text-slate-700">Type</span>
          <select
            name="type"
            value={filter.type}
            onChange={onFilterChange}
            disabled={loadingTrans}
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20 disabled:bg-slate-50 disabled:text-slate-500"
          >
            <option value="">All</option>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>
        </label>

        {/* Category */}
        <label className="flex flex-col">
          <span className="mb-1 text-sm font-medium text-slate-700">Category</span>
          <select
            name="categoryId"
            value={filter.categoryId}
            onChange={onFilterChange}
            disabled={loadingCats || !!catsError || loadingTrans}
            aria-invalid={!!catsError}
            className={`h-9 rounded-md border bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:ring-2 disabled:bg-slate-50 disabled:text-slate-500
              ${catsError ? "border-red-500 focus:ring-red-500/20" : "border-slate-300 focus:border-blue-600 focus:ring-blue-600/20"}`}
          >
            <option value="">All</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          {catsError && (
            <button
              type="button"
              onClick={refreshCats}
              className="mt-1 inline-flex text-sm text-blue-600 hover:underline"
            >
              Retry categories
            </button>
          )}
        </label>

        {/* Start */}
        <label className="flex flex-col">
          <span className="mb-1 text-sm font-medium text-slate-700">Start</span>
          <input
            type="date"
            name="startDate"
            value={filter.startDate}
            onChange={onFilterChange}
            disabled={loadingTrans}
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20 disabled:bg-slate-50 disabled:text-slate-500"
          />
        </label>

        {/* End */}
        <label className="flex flex-col">
          <span className="mb-1 text-sm font-medium text-slate-700">End</span>
          <input
            type="date"
            name="endDate"
            value={filter.endDate}
            onChange={onFilterChange}
            min={filter.startDate || undefined}
            disabled={loadingTrans}
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20 disabled:bg-slate-50 disabled:text-slate-500"
          />
        </label>

        {/* Buttons */}
        <div className="flex items-end gap-2">
          <button
            type="submit"
            disabled={loadingTrans}
            className="inline-flex h-9 items-center justify-center rounded-md bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:opacity-60"
          >
            Apply
          </button>
          <button
            type="button"
            disabled={loadingTrans}
            onClick={() => {
              clearFilter();
              fetchAll();
            }}
            className="inline-flex h-9 items-center justify-center rounded-md border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-900 shadow-sm transition hover:bg-slate-100 disabled:opacity-60"
          >
            Clear
          </button>
        </div>
      </form>

      {/* Table */}
      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <table className="min-w-full text-left">
          <caption className="sr-only">List of transactions</caption>
          <thead className="bg-slate-50 text-slate-700">
            <tr>
              {columns.map((c) => {
                const isActive = sort.key === c.id;
                const caret = isActive ? (sort.dir === "asc" ? <ChevronUp /> : <ChevronDown />) : <ChevronsUpDown />;
                return (
                  <th
                    key={c.id}
                    scope="col"
                    aria-sort={ariaSortFor(c.id)}
                    className={`px-4 py-3 text-sm font-semibold ${c.align === "right" ? "text-right" : ""}`}
                  >
                    {c.sortable ? (
                      <button
                        type="button"
                        onClick={() => onSortClick(c)}
                        aria-label={`Sort by ${c.header} ${
                          isActive ? (sort.dir === "asc" ? "descending" : "ascending") : c.defaultDir ?? "ascending"
                        }`}
                        className={`group inline-flex items-center gap-1 ${
                          isActive ? "text-blue-700" : "text-slate-700"
                        }`}
                      >
                        <span>{c.header}</span>
                        <span className="inline-flex" aria-hidden="true">
                          {caret}
                        </span>
                      </button>
                    ) : (
                      c.header
                    )}
                  </th>
                );
              })}
            </tr>
          </thead>

          <tbody className="divide-y divide-slate-200" aria-busy={loadingTrans}>
            {view === "error" && (
              <tr>
                <td colSpan={colCount} className="px-4 py-6">
                  <div
                    role="alert"
                    className="flex items-center justify-between rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
                  >
                    <p className="m-0">
                      <strong className="font-semibold">Error:</strong> {transError}
                    </p>
                    <button
                      className="inline-flex h-8 items-center justify-center rounded-md bg-blue-600 px-3 text-xs font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:opacity-60"
                      onClick={fetchAll}
                      disabled={loadingTrans}
                    >
                      Retry
                    </button>
                  </div>
                </td>
              </tr>
            )}

            {view === "loading" && (
              <tr>
                <td colSpan={colCount} className="px-4 py-6 text-sm text-slate-600">
                  Loading transactions...
                </td>
              </tr>
            )}

            {view === "empty" && (
              <tr>
                <td colSpan={colCount} className="px-4 py-6 text-sm text-slate-600">
                  No transactions found.
                </td>
              </tr>
            )}

            {view === "data" &&
              rows.map(({ id, amount, date, description, categoryId, category }) => (
                <tr key={id} className="hover:bg-slate-50/60">
                  <td className="px-4 py-3 text-sm text-slate-900">
                    {dateFormatter.format(new Date(date + "T00:00:00"))}
                  </td>
                  <td className="px-4 py-3 text-sm text-slate-900">{description}</td>
                  <td className="px-4 py-3 text-sm text-slate-900">
                    {byId.get(String(categoryId))?.name ?? category?.name ?? "—"}
                  </td>
                  <td className={`px-4 py-3 text-sm text-slate-900 ${amountAlign}`}>
                    {currencyFormatter.format(amount ?? 0)}
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default TransactionPage;