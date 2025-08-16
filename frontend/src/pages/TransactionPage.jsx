import { useEffect, useState, useRef, useCallback, useMemo } from "react";
import { useAuth } from "../hooks/useAuth";
import api from "../api/api";
import { ChevronUp, ChevronDown, ChevronsUpDown, ScrollText } from "lucide-react";
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
        err?.response?.date?.message ||
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
  const views = {
    error: <tr>
            <td colSpan={colCount}>
              <div className="alert alert--error flex items-center justify-between" role="alert">
                <p className="m-0"><strong>Error:</strong> {transError}</p>
                <button className="btn btn--primary" onClick={fetchAll} disabled={loadingTrans}>Retry</button>
              </div>
            </td>
          </tr>,
    loading: <tr><td colSpan={colCount}>Loading transactions...</td></tr>,
    empty: <tr><td colSpan={colCount}>No transactions found.</td></tr>,
    data: rows.map(({ id, amount, date, description, category }) => (
            <tr key={id}>
              <td>{dateFormatter.format(new Date(date + "T00:00:00"))}</td>
              <td>{description}</td>
              <td>{category?.name ?? "-"}</td>
              <td className={amountAlign}>{currencyFormatter.format(amount ?? 0)}</td>
            </tr>  
          )),
  };

  const ariaSortFor = (id) =>
    sort.key === id ? (sort.dir === "asc" ? "ascending" : "descending") : "none";

  const onSortClick = (col) =>
    setSort((s) =>
      s.key === col.id
        ? { key: col.id, dir: s.dir === "asc" ? "desc" : "asc" }
        : { key: col.id, dir: col.defaultDir ?? "asc" }
    );
  
  return (
    <div className="container mt-16 mb-24">
      <h2>Transactions</h2>

      {/* Filter Form */}
      <form
        className="mb-4 grid gap-2 md:grid-cols-5"
        onSubmit={(e) => {
          e.preventDefault();
          const anyFilter = filter.type || filter.categoryId || filter.startDate || filter.endDate;
          anyFilter ? fetchFiltered() : fetchAll();
        }}
      >
        <label className="flex flex-col">
          <span className="mb-1">Type</span>
          <select
            name="type"
            value={filter.type}
            onChange={onFilterChange}
            disabled={loadingTrans}
          >
            <option value="">All</option>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>
        </label>

        <label className="flex flex-col">
          <span className="mb-1">Category</span>
          <select
            name="categoryId"
            value={filter.categoryId}
            onChange={onFilterChange}
            disabled={loadingCats || !!catsError || loadingTrans}
            aria-invalid={!!catsError}
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
              className="btn btn--link mt-1 p-0"
              onClick={refreshCats}
            >
              Retry categories
            </button>
          )}
        </label>

        <label className="flex flex-col">
          <span className="mb-1">Start</span>
          <input
            type="date"
            name="startDate"
            value={filter.startDate}
            onChange={onFilterChange}
            disabled={loadingTrans}
          />
        </label>

        <label className="flex flex-col">
          <span className="mb-1">End</span>
          <input
            type="date"
            name="endDate"
            value={filter.endDate}
            onChange={onFilterChange}
            min={filter.startDate || undefined}
            disabled={loadingTrans}
          />
        </label>

        <div className="flex items-end gap-2">
          <button type="submit" className="btn btn--primary" disabled={loadingTrans}>
            Apply
          </button>
          <button
            type="button"
            className="btn"
            disabled={loadingTrans}
            onClick={() => {
              clearFilter();
              fetchAll();
            }}
          >
            Clear
          </button>
        </div>
      </form>
      <table className="table">
        <caption className="sr-only">List of transactions</caption>
        <thead>
          <tr>
            {columns.map((c) => {
              const isActive = sort.key === c.id;
              const caret = isActive ? (sort.dir === "asc" ? <ChevronUp /> : <ChevronDown />) : <ChevronsUpDown />;
              return (
                <th 
                  key={c.id} 
                  scope="col"
                  aria-sort={ariaSortFor(c.id)}
                  className={`${c.align === "right" ? "text-right" : ""} ${isActive ? "th--active" : ""}`} 
                >
                  {c.sortable ? (
                    <button
                      type="button"
                      className="sort-btn"
                      onClick={() => onSortClick(c)}
                      aria-label={`Sort by ${c.header} ${isActive ? (sort.dir === "asc" ? "descending" : "ascending") : (c.defaultDir ?? "ascending")}`}
                    >
                      <span>{c.header}</span>
                      <span className="sort-indicator" aria-hidden="true">{caret}</span>
                    </button>
                  ) : (
                    c.header
                  )}
              </th>
              );
            })}
          </tr>
        </thead>

        <tbody aria-busy={loadingTrans}>
          {views[view]}
        </tbody>
      </table>
    </div>
  )
}

export default TransactionPage;