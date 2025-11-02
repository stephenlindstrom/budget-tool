import { useEffect, useState, useRef, useCallback, useMemo } from "react";
import { useAuth } from "../hooks/useAuth";
import api from "../api/api";
import { ChevronUp, ChevronDown, ChevronsUpDown, Plus, Trash, Pencil } from "lucide-react";
import { useCategories } from "../hooks/useCategories";
import TransactionModal from "../components/transactions/TransactionModal";
import ConfirmDialog from "../components/common/ConfirmDialog";

function TransactionPage() {
  const { loading: authLoading } = useAuth();

  // ---- Categories (from context) ----
  const {
    categories,
    loading: loadingCats,
    error: catsError,
    refresh: refreshCats
  } = useCategories();

  // ---- Transactions state ----
  const [transactions, setTransactions] = useState([]);
  const [transError, setTransError] = useState("");
  const [loadingTrans, setLoadingTrans] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  // ---- Delete confirmation modal ----
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [confirmTarget, setConfirmTarget] = useState(null);
  const [confirmLoading, setConfirmLoading] = useState(false);

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

  function formatDisplayDateLong(dateStr) {
    if (!dateStr) return "-";
    const [y, m, d] = dateStr.split("-");
    const months = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];
    return `${months[m-1]} ${d} ${y}`;
  }

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

  const openDeleteConfirm = useCallback((id) => {
    const tx = transactions.find(t => t.id === id);
    if (!tx) return;
    setConfirmTarget({ id: tx.id, description: tx.description, amount: tx.amount });
    setConfirmOpen(true);
  }, [transactions]);

  const handleConfirmDelete = useCallback(async () => {
    if (!confirmTarget) return;
    const id = confirmTarget.id;
    const tx = transactions.find(t => t.id === id);
    if (!tx) { setConfirmOpen(false); return; }

    setDeletingId(id);
    setConfirmLoading(true);
    setTransError("");

    // optimistic remove
    setTransactions(list => list.filter(t => t.id !== id));

    try {
      await api.delete(`/transactions/${id}`);
    } catch (err) {
      // rollback on failure
      setTransactions(list => {
        // reinsert and let existing sort logic reorder it
        return [...list, tx];
      });
      const msg =
        err?.response?.data?.message ||
        (err?.code === "ERR_NETWORK" ? "Network error. Check your connection." : "Delete failed");
      setTransError(msg);
    } finally {
      setDeletingId(null);
      setConfirmLoading(false);
      setConfirmOpen(false);
      setConfirmTarget(null);
    }
  }, [confirmTarget, transactions]);

  // initial load
  useEffect(() => {
    if (!authLoading) fetchAll();
    return () => transCtrlRef.current?.abort();
  }, [authLoading, fetchAll]);

  // ---- Create/Edit Transaction Modals ----
  const [openAdd, setOpenAdd] = useState(false);
  const [openEdit, setOpenEdit] = useState(false);
  const [selectedTx, setSelectedTx] = useState(null);

  const matchesFilter = useCallback(
    (tx) => {
      if (!tx) return false;
      if (filter.type && tx.type !== filter.type) return false;
      if (filter.categoryId && String(tx.category?.id ?? tx.categoryId) !== String(filter.categoryId))
        return false;
      if (filter.startDate && (tx.date ?? "") < filter.startDate) return false;
      if (filter.endDate && (tx.date ?? "") > filter.endDate) return false;
      return true;
    },
    [filter]
  );

  const rows = useMemo(() => {
    const mult = sort.dir === "asc" ? 1 : -1;
    const cmp = (a, b) => {
      const val = sort.key === "date"
        ? Date.parse(`${a?.date ?? ""}T00:00:00`) - Date.parse(`${b?.date ?? ""}T00:00:00`)
        : sort.key === "amount"
        ? (Number(a?.amount)||0) - (Number(b?.amount)||0)
        : sort.key === "category"
        ? (a?.category?.name ?? "").localeCompare(b?.category?.name ?? "",
          undefined,
          { sensitivity: "base" }
        )
        : (a?.description ?? "").localeCompare(b?.description ?? "", undefined, { sensitivity: "base" });
      
      return val * mult;
    };
    return [...transactions].sort(cmp);
  }, [transactions, sort]);

  const columns = [
    { id: "date", header: "Date", sortable: true, defaultDir: "desc" },
    { id: "description", header: "Description", sortable: true, defaultDir: "asc"},
    { id: "category", header: "Category", sortable: true, defaultDir: "asc"},
    { id: "amount", header: "Amount", align: "right", sortable: true, defaultDir: "desc" },
    { id: "actions", header: "", sortable: false },
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

  const anyFilterActive = filter.type || filter.categoryId || filter.startDate || filter.endDate;

  const visibleCategories = useMemo(
    () => (!filter.type ? categories : categories.filter(c => c.type === filter.type)),
    [categories, filter.type]
  );

  useEffect(() => {
    if (!filter.categoryId) return;
    const stillValid = visibleCategories.some(c => String(c.id) === String(filter.categoryId));
    if (!stillValid) {
      setFilter(f => ({...f, categoryId: "" }));
    }
  }, [visibleCategories, filter.categoryId]);

  return (
    <div className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8 mt-16 mb-24">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-2xl font-semibold tracking-tight text-slate-900">
          Transactions
        </h2>
        <button
          type="button"
          onClick={() => setOpenAdd(true)}
          className="inline-flex h-9 items-center gap-2 rounded-md bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 cursor-pointer"
        >
          <Plus size={16} />
          Add transaction
        </button>
      </div>

      {/* Filter Form */}
      <form
        className="grid gap-3 md:grid-cols-5 items-end mb-6"
        onSubmit={(e) => {
          e.preventDefault();
          anyFilterActive ? fetchFiltered() : fetchAll();
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
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20 disabled:bg-slate-50 disabled:text-slate-500 cursor-pointer"
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
            className={`h-9 rounded-md border bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:ring-2 disabled:bg-slate-50 disabled:text-slate-500 cursor-pointer
              ${catsError ? "border-red-500 focus:ring-red-500/20" : "border-slate-300 focus:border-blue-600 focus:ring-blue-600/20"}`}
          >
            <option value="">All</option>
            {visibleCategories.map((c) => (
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
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20 disabled:bg-slate-50 disabled:text-slate-50 cursor-pointer"
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
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20 disabled:bg-slate-50 disabled:text-slate-500 cursor-pointer"
          />
        </label>

        {/* Buttons */}
        <div className="flex items-end gap-2">
          <button
            type="submit"
            disabled={loadingTrans}
            className="inline-flex h-9 items-center justify-center rounded-md bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:opacity-60 cursor-pointer"
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
            className="inline-flex h-9 items-center justify-center rounded-md border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-900 shadow-sm transition hover:bg-slate-100 disabled:opacity-60 cursor-pointer"
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
                        className={`group inline-flex items-center gap-1 cursor-pointer ${
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
                      onClick={anyFilterActive ? fetchFiltered : fetchAll}
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
              rows.map(({ id, amount, date, description, category }) => (
                <tr key={id} className="hover:bg-slate-50/60">
                  <td className="px-4 py-3 text-sm text-slate-900">
                    {formatDisplayDateLong(date)}
                  </td>
                  <td className="px-4 py-3 text-sm text-slate-900">{description ?? "-"}</td>
                  <td className="px-4 py-3 text-sm text-slate-900">
                    {category?.name ?? "—"}
                  </td>
                  <td className={`px-4 py-3 text-sm text-slate-900 ${amountAlign}`}>
                    {currencyFormatter.format(Number(amount ?? 0))}
                  </td>
                  <td className="px-2 py-2 text-sm">
                    <div className="flex items-center justify-center gap-2">
                      <button
                        type="button"
                        onClick={() => {
                          setSelectedTx({ id, amount, date, description, category, categoryId: category?.id, type: rows.find(r => r.id === id)?.type });
                          setOpenEdit(true);
                        }}
                        disabled={loadingTrans}
                        aria-label={`Edit ${description ?? "transaction"}`}
                        title="Edit transaction"
                        className="inline-flex h-8 w-8 items-center justify-center rounded-md hover:bg-slate-100 disabled:opacity-50 cursor-pointer mr-1.5"
                      >
                        <Pencil size={16} />
                      </button>
                      <button
                        type="button"
                        onClick={() => openDeleteConfirm(id)}
                        disabled={deletingId === id || loadingTrans}
                        aria-label={`Delete ${description ?? "transaction"}`}
                        title={deletingId === id ? "Deleting..." : "Delete transaction"}
                        className="inline-flex h-8 w-8 items-center justify-center rounded-md hover:bg-red-50 disabled:opacity-50 cursor-pointer"
                      >
                        <Trash size={16}/>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>

      {/* Create Transaction Modal */}
      <TransactionModal
        open={openAdd}
        mode="create"
        onClose={() => setOpenAdd(false)}
        onSaved={(tx) => {
          if (!tx) return;
          if (!anyFilterActive || matchesFilter(tx)) {
              setTransactions((list) => [tx, ...list]);
          }
        }}
      />

      {/* Edit Transaction Modal */}
      <TransactionModal
        open={openEdit}
        mode="edit"
        initial={selectedTx}
        onClose={() => {
          setOpenEdit(false);
          setSelectedTx(null);
        }}
        onSaved={(tx) => {
          if (!tx) return;

          setTransactions((list) => {
            // first, merge the updated tx
            const merged = list.map((t) => (t.id === tx.id ? { ...t, ...tx } : t));

            // if a filter is active and this edit breaks the match, drop it locally
            if (anyFilterActive && !matchesFilter(tx)) {
              return merged.filter((t) => t.id !== tx.id);
            }
            return merged;
          });
        }}

      />

      {/* Delete Confirmation Modal */}
      <ConfirmDialog
        open={confirmOpen}
        title="Delete transaction?"
        body={
          confirmTarget?.description && confirmTarget?.amount
            ? `"${confirmTarget.description} - ${currencyFormatter.format(Number(confirmTarget.amount))}" will be permanently removed.`
            : "This transaction will be permanently removed."
        }
        confirmText="Delete"
        cancelText="Cancel"
        destructive
        loading={confirmLoading}
        onCancel={() => { if (!confirmLoading) { setConfirmOpen(false); setConfirmTarget(null); } }}
        onConfirm={handleConfirmDelete}
      />
    </div>
  );
}

export default TransactionPage;