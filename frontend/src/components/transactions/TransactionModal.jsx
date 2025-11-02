import { useEffect, useState, useRef, useMemo } from "react";
import { useCategories } from "../../hooks/useCategories";
import api from "../../api/api";
import CategoryModal from "../categories/CategoryModal";

export default function TransactionModal({
  open,
  onClose,
  // "create" | "edit"
  mode = "create",
  // when editing, pass the transaction you’re editing
  initial = null,
  // onSaved(entity) fires with created/updated transaction
  onSaved,
}) {
  const isEdit = mode === "edit";

  const {
    categories,
    loading: loadingCats,
    error: catsError,
    refresh: refreshCats,
  } = useCategories();

  const [showCatModal, setShowCatModal] = useState(false);

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

  // Populate form on open (especially for edit)
  useEffect(() => {
    if (!open) return;

    if (isEdit && initial) {
      setForm({
        date: initial.date ?? "",
        description: initial.description ?? "",
        amount:
          initial.amount != null && Number.isFinite(Number(initial.amount))
            ? String(initial.amount)
            : "",
        type: initial.type ?? "EXPENSE",
        categoryId:
          initial.categoryId != null ? String(initial.categoryId) : "",
      });
    } else {
      // reset for create
      setForm({
        date: "",
        description: "",
        amount: "",
        type: "EXPENSE",
        categoryId: "",
      });
    }
  }, [open, isEdit, initial]);

  // autofocus
  useEffect(() => {
    if (open) {
      setTimeout(() => firstFieldRef.current?.focus(), 0);
    }
  }, [open]);

  // esc to close (disabled while child modal open)
  useEffect(() => {
    if (!open) return;
    const onKey = (e) => {
      if (e.key === "Escape" && !showCatModal) onClose?.();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose, showCatModal]);

  const visibleCategories = useMemo(
    () =>
      !form.type
        ? categories
        : categories.filter((c) => c.type === form.type),
    [categories, form.type]
  );

  // if user switches type, ensure category still valid
  useEffect(() => {
    if (!form.categoryId) return;
    const ok = visibleCategories.some(
      (c) => String(c.id) === String(form.categoryId)
    );
    if (!ok) {
      setForm((f) => ({ ...f, categoryId: "" }));
    }
  }, [visibleCategories, form.categoryId]);

  const onChange = (e) =>
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const handleCreateCategory = async (name, type) => {
    const res = await api.post("/categories", { name: name.trim(), type });
    const created = res.data;
    await refreshCats();

    setForm((f) => {
      const next = { ...f };
      if (created?.type !== f.type) next.type = created.type;
      next.categoryId = String(created.id);
      return next;
    });

    return created;
  };

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
      let res;
      if (isEdit && initial?.id != null) {
        res = await api.put(`/transactions/${initial.id}`, payload);
      } else {
        res = await api.post("/transactions", payload);
      }
      onSaved?.(res.data);
      onClose?.();

      // clear only after success
      setForm({
        date: "",
        description: "",
        amount: "",
        type: "EXPENSE",
        categoryId: "",
      });
    } catch (err) {
      const status = err?.response?.status;
      const serverMsg = err?.response?.data?.message;

      if ((status === 422 || status === 400) && serverMsg) {
        setError(serverMsg);
      } else {
        setError(
          err?.code === "ERR_NETWORK"
            ? "Network error. Check your connection."
            : isEdit
            ? "Error updating transaction"
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
        if (showCatModal) return;
        if (e.target === e.currentTarget) onClose?.();
      }}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="tx-modal-title"
        className="w-full max-w-md rounded-xl bg-white p-5 shadow-lg"
      >
        <h3 id="tx-modal-title" className="mb-3 text-lg font-semibold">
          {isEdit ? "Edit transaction" : "Add transaction"}
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
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20 cursor-pointer"
            type="date"
            name="date"
            value={form.date}
            onChange={onChange}
          />

          {/* description (optional) */}
          <input
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20"
            name="description"
            placeholder="Description (optional)"
            value={form.description}
            onChange={onChange}
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

          {/* type (required) */}
          <select
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20 cursor-pointer"
            name="type"
            value={form.type}
            onChange={onChange}
            required
          >
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>

          {/* categoryId (required) + New Category */}
          <div className="grid grid-cols-[1fr_auto] items-center gap-2">
            <select
              className={`h-9 rounded-md border bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:ring-2 disabled:bg-slate-50 disabled:text-slate-500 cursor-pointer ${
                catsError
                  ? "border-red-500 focus:ring-red-500/20"
                  : "border-slate-300 focus:border-blue-600 focus:ring-blue-600/20"
              }`}
              name="categoryId"
              value={form.categoryId}
              onChange={onChange}
              disabled={loadingCats || visibleCategories.length === 0}
              required
            >
              <option value="">
                {visibleCategories.length === 0
                  ? "No categories for this type"
                  : "Select category"}
              </option>
              {visibleCategories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>

            <button
              type="button"
              onClick={() => setShowCatModal(true)}
              className="h-9 rounded-md border border-slate-300 bg-white px-3 text-sm font-semibold text-slate-900 shadow-sm transition hover:bg-slate-100 cursor-pointer"
              aria-label="Create new category"
            >
              + New
            </button>
          </div>

          <div className="mt-2 flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="h-9 rounded-md border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-900 shadow-sm transition hover:bg-slate-100 cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting || amountInvalid || categoryMissing}
              className="h-9 rounded-md bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:opacity-60 cursor-pointer"
            >
              {submitting ? (isEdit ? "Updating..." : "Saving...") : isEdit ? "Update" : "Save"}
            </button>
          </div>
        </form>
      </div>

      {/* Child modal for new category */}
      <CategoryModal
        isOpen={showCatModal}
        onClose={() => setShowCatModal(false)}
        onCreate={async (name, type) => {
          const created = await handleCreateCategory(name, type);
          setShowCatModal(false);
          return created;
        }}
        categories={categories}
        initialName=""
        initialType={form.type}
      />
    </div>
  );
}
