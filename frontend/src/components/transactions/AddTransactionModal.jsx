import { useEffect, useState, useRef, useMemo } from "react";
import { useCategories } from "../../hooks/useCategories";
import api from "../../api/api";
import { Filter } from "lucide-react";

function AddTransactionModal({ open, onClose, onCreated }) {
  const { categories, loading: loadingCats, error: catsError, refresh: refreshCats } = useCategories();
  const [form, setForm] = useState({ 
    date: "", 
    description: "", 
    amount: "", 
    type: "EXPENSE", 
    categoryId: "",
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const firstFieldRef = useRef(null);
  useEffect(() => {
    if (open) {
      setTimeout(() => firstFieldRef.current?.focus(), 0);
    }
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const onKey = (e) => e.key === "Escape" && onClose();
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  const visibleCategories = useMemo(
    () => (!Filter.type ? categories : categories.filter(c => c.type === form.type)),
    [categories, form.type]
  ); 

  useEffect(() => {
    if (!form.categoryId) return;
    const ok = visibleCategories.some(c => String(c.id) === String(form.categoryId));
    if (!ok) {
      setForm(f => ({ ...f, categoryId: ""}));
    }
  }, [visibleCategories, form.categoryId]);

  const onChange = (e) => setForm(f => ({...f, [e.target.name]: e.target.value }));

  const amountNum = Number(form.amount);
  const amountInvalid = !Number.isFinite(amountNum) || amountNum < 0;
  const categoryMissing = !form.categoryId;

  const onSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError("");

    if (amountInvalid) {
      setSubmitting(false);
      setError("Amount must be a number >= 0.");
      return;
    }
    if (categoryMissing) {
      setSubmitting(false);
      setError("Please select a category.");
      return;
    }

    const payload = {
      amount: amountNum,
      categoryId: Number(form.categoryId),
      type: form.type,
      date: form.date || null,
      description: form.description?.trim() || null,
    };

    if (payload.date == null) delete payload.date;
    if (payload.description == null) delete payload.description;

    try {
      const res = await api.post("/transactions", payload);
      onCreated?.(res.data);
      onClose();
      setForm({ date: "", description: "", amount: "", type: "EXPENSE", categoryId: ""});
    } catch (err) {
      const status = err?.response?.status;
      const serverMsg = err?.response?.data?.message;

      if (status === 422 && serverMsg) {
        setError(serverMsg);
      } else if (status === 400 && serverMsg) {
        setError(serverMsg)
      } else {
        setError( 
          err?.code === "ERR_NETWORK" 
          ? "Network error. Check your connection." 
          : "Error creating transaction"
        );  
      }
      
    } finally {
      setSubmitting(false);
    }
  };

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      onMouseDown={(e) => {
        // click outside to close (ignore clicks inside the dialog)
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="add-tx-title"
        className="w-full max-w-md rounded-xl bg-white p-5 shadow-lg"
      >
        <h3 id="add-tx-title" className="mb-3 text-lg font-semibold">
          Add transaction
        </h3>

        {error && <p className="mb-2 text-sm text-red-600">{error}</p>}
        {catsError && (
          <div className="mb-2 text-sm text-red-600">
            {catsError}{" "}
            <button
              type="button"
              onClick={refreshCats}
              className="text-blue-600 underline hover:no-underline"
            >
              Retry
            </button>
          </div>
        )}

        <form onSubmit={onSubmit} className="grid gap-3">
          {/* date (optional) */}
          <input
            ref={firstFieldRef}
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20"
            type="date"
            name="date"
            value={form.date}
            onChange={onChange}
            // no required — backend defaults to today
          />

          {/* description (optional) */}
          <input
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20"
            name="description"
            placeholder="Description (optional)"
            value={form.description}
            onChange={onChange}
            // no required — backend allows null/blank
          />

          {/* amount (required) */}
          <input
            className={`h-9 rounded-md border px-3 text-slate-900 shadow-sm outline-none transition focus:ring-2 ${
              amountInvalid
                ? "border-red-500 focus:ring-red-500/20"
                : "border-slate-300 focus:border-blue-600 focus:ring-blue-600/20"
            }`}
            type="number"
            inputMode="decimal"
            min="0"
            step="0.01"
            name="amount"
            placeholder="Amount"
            value={form.amount}
            onChange={onChange}
            required
          />

          {/* type (required, default EXPENSE) */}
          <select
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20"
            name="type"
            value={form.type}
            onChange={onChange}
            required
          >
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>

          {/* categoryId (required) */}
          <select
            className={`h-9 rounded-md border bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:ring-2 disabled:bg-slate-50 disabled:text-slate-500 ${
              catsError ? "border-red-500 focus:ring-red-500/20" : "border-slate-300 focus:border-blue-600 focus:ring-blue-600/20"
            }`}
            name="categoryId"
            value={form.categoryId}
            onChange={onChange}
            disabled={loadingCats || visibleCategories.length === 0}
            required
          >
            <option value="">
              {visibleCategories.length === 0 ? "No categories for this type" : "Select category"}
            </option>
            {visibleCategories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>

          <div className="mt-2 flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="h-9 rounded-md border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-900 shadow-sm transition hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting || amountInvalid || categoryMissing}
              className="h-9 rounded-md bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:opacity-60"
            >
              {submitting ? "Saving..." : "Save"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddTransactionModal;