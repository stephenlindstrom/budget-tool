import { useEffect, useState, useRef, useCallback, useMemo } from "react";
import { useAuth } from "../hooks/useAuth";
import api from "../api/api";
import { ChevronUp, ChevronDown, ChevronsUpDown, ScrollText } from "lucide-react";

function TransactionPage() {
  const { loading } = useAuth();

  const [transactions, setTransactions] = useState([]);
  const [transError, setTransError] = useState("");
  const [loadingTrans, setLoadingTrans] = useState(false);
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

  const controllerRef = useRef(null);

  const fetchTransactions = useCallback(async () => {
    controllerRef.current?.abort();
    const controller = new AbortController();
    controllerRef.current = controller;

    setLoadingTrans(true);
    setTransError("");

    try {
        const res = await api.get("/transactions", { signal: controller.signal });
        setTransactions(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        if (controller.signal.aborted) return;
        console.error("Fetch /transactions failed:", err?.response?.status, err);
        const msg =
          err?.response?.data?.message || (err?.code === "ERR_NETWORK" ? "Network error. Check your connection." : "Error fetching transactions");
        setTransError(msg);
      } finally {
        if (!controller.signal.aborted) setLoadingTrans(false);
      }
  }, []);

  useEffect(() => {
    if (!loading) fetchTransactions();
    return () => controllerRef.current?.abort();
  }, [loading, fetchTransactions]);

  const rows = useMemo(() => {
    const mult = sort.dir === "asc" ? 1 : -1;
    const cmp = (a, b) => {
      const val = sort.key === "date"
        ? Date.parse(`${a.date}T00:00:00`) - Date.parse(`${b.date}T00:00:00`)
        : sort.key === "amount"
        ? (Number(a.amount)||0) - (Number(b.amount)||0)
        : (a.description||"").localeCompare(b.description||"", undefined, { sensitivity: "base" });
      
      return val * mult;
    };
    return [...transactions].sort(cmp);
  }, [transactions, sort]);

  const columns = [
    { id: "date", header: "Date", sortable: true, defaultDir: "desc" },
    { id: "description", header: "Description", sortable: true, defaultDir: "asc"},
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
                <button className="btn btn--primary" onClick={fetchTransactions} disabled={loadingTrans}>Retry</button>
              </div>
            </td>
          </tr>,
    loading: <tr><td colSpan={colCount}>Loading transactions...</td></tr>,
    empty: <tr><td colSpan={colCount}>No transactions found.</td></tr>,
    data: rows.map(({ id, amount, date, description }) => (
            <tr key={id}>
              <td>{dateFormatter.format(new Date(date + "T00:00:00"))}</td>
              <td>{description}</td>
              <td className={amountAlign}>{currencyFormatter.format(amount ?? 0)}</td>
            </tr>  
          ))
  }

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