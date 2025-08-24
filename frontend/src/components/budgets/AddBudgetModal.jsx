import { useEffect, useMemo, useRef, useState } from "react";
import { useCategories } from "../../hooks/useCategories";
import api from "../../api/api";
import CategoryModal from "../categories/CategoryModal";

function AddBudgetModal({ open, onClose, onCreated, defaultMonth }) {
  const { categories, loading: loadingCats, error: catsError, refresh: refreshCats } = useCategories();

  const [showCatModal, setShowCatModal] = useState(false);

  const [form, setForm] = useState({
    value: "",
    month: defaultMonth ?? new Date().toISOString().slice(0, 7),
    categoryId: "",
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const firstFieldRef = useRef(null);
  const prevFocusRef = useRef(null);
  const dialogRef = useRef(null);

  useEffect(() => {
    if (!open) return;
    setError("");
    setTimeout(() => firstFieldRef.current?.focus(), 0);
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const container = dialogRef.current;
    if (!container) return;

    prevFocusRef.current = document.activeElement;

    const getFocusables = () =>
      Array.from(
        container?.querySelectorAll(
          'a[href], button, textarea, input, select, [tabindex]:not([tabindex="-1"])'
        ) || []
      ).filter(el =>
        !el.hasAttribute('disabled') &&
        el.getAttribute('aria-hidden') !== 'true' &&
        el.type !== 'hidden' &&
        (el.offsetParent !== null || el.getClientRects().length > 0)
      );

    function handleKeyDown(e) {
      if (showCatModal) return;
      if (e.key === "Escape") {
        e.stopPropagation();
        if (!submitting) onClose();
        return;
      }
      if (e.key !== "Tab") return;

      const focusables = getFocusables();
      if (focusables.length === 0) {
        container.tabIndex = -1;
        container.focus();
        e.preventDefault();
        return;
      }

      const first = focusables[0];
      const last = focusables[focusables.length - 1];

      if (e.shiftKey) {
        if (document.activeElement === first || !container.contains(document.activeElement)) {
          e.preventDefault();
          last.focus();
        }
      } else {
        if (document.activeElement === last || !container.contains(document.activeElement)) {
          e.preventDefault();
          first.focus();
        }
      }
    }

    function handleFocusIn(e) {
      if (showCatModal) return;
      if (!container.contains(e.target)) {
        const [first] = getFocusables();
        (first || container).focus();
        e.stopPropagation();
      }
    }

    document.addEventListener("keydown", handleKeyDown, true);
    document.addEventListener("focusin", handleFocusIn, true);

    return () => {
      document.removeEventListener("keydown", handleKeyDown, true);
      document.removeEventListener("focusin", handleFocusIn, true);
      prevFocusRef.current?.focus?.();
    };
  }, [open, submitting, onClose, showCatModal]);

  useEffect(() => {
      if (!open) return;
      const { style } = document.body;
      const prev = style.overflow;
      style.overflow = 'hidden';
      return () => { style.overflow = prev; };
    }, [open]);

  const visibleCategories = useMemo(
    () => categories.filter((c) => c.type === "EXPENSE"),
    [categories]
  );

  const onChange = (e) => setForm(f => ({...f, [e.target.name]: e.target.value }));

  const handleCreateCategory = async (name, type) => {
    try {
      const res = await api.post("/categories", {name: name.trim(), type });
      const created = res.data;
      await refreshCats();

      setForm(f => created?.type === "EXPENSE" ? { ...f, categoryId: String(created.id) } : f);

      return created;
    } catch (err) {
      const msg =
        err?.response?.data?.message ??
        (err?.code === "ERR_NETWORK" ? "Network error. Check your connection." : "Could not create category.");
      const e = new Error(msg);
      e.cause = err;
      throw e;
    }
  };

  const valueNum = Number(form.value);
  const valueInvalid = form.value === "" || !Number.isFinite(valueNum) || valueNum < 0;
  const categoryMissing = !form.categoryId;

  const onSubmit = async (e) => {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);
    setError("");

    if (valueInvalid) {
      setSubmitting(false);
      setError("Value must be a number >= 0.");
      return;
    }
    if (categoryMissing) {
      setSubmitting(false);
      setError("Please select a category.");
      return;
    }

    const toCents = (n) => Math.round(n * 100) / 100;

    const payload = {
      value: toCents(valueNum),
      month: form.month,
      categoryId: Number(form.categoryId),
    };

    try {
      const res = await api.post("/budgets", payload);
      onCreated?.(res.data);
      onClose();
      setForm({ value: "", month: defaultMonth ?? new Date().toISOString().slice(0, 7), categoryId: ""});
    } catch (err) {
      const status = err?.response?.status;
      const serverMsg = err?.response?.data?.message;

      if (status === 422 && serverMsg) {
        setError(serverMsg);
      } else if (status === 400 && serverMsg) {
        setError(serverMsg);
      } else if (status === 409 && serverMsg) {
        setError(serverMsg);
      } else {
        setError(
          err?.code === "ERR_NETWORK"
          ? "Network error. Check your connection."
          : "Error creating budget"
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
        if (showCatModal) return;
        if (e.target === e.currentTarget && !submitting) onClose();
      }}
    >
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="add-budget-title"
        aria-busy={submitting}
        className="w-full max-w-md rounded-xl bg-white p-5 shadow-lg"
      >
        <h3 id="add-budget-title" className="mb-3 text-lg font-semibold">
          Add budget
        </h3>

        {error && (
          <p className="mb-2 text-sm text-red-600" role="alert" id="add-budget-error">
            {error}
          </p>
        )}
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

        <form onSubmit={onSubmit} className="grid gap-3" aria-busy={loadingCats}>
          {/* month (required) */}
          <label htmlFor="month" className="sr-only">Month</label>
          <input
            id="month" aria-describedby={error ? "add-budget-error" : undefined}
            className="h-9 rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20"
            type="month"
            name="month"
            value={form.month}
            onChange={onChange}
            required
          />

          {/* categoryId (required) + New Category */}
          <div className="grid grid-cols-[1fr_auto] items-center gap-2">
            <label htmlFor="categoryId" className="sr-only">Category</label>
            <select
              className={`h-9 rounded-md border bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:ring-2 disabled:bg-slate-50 disabled:text-slate-500 ${
                catsError 
                  ? "border-red-500 focus:ring-red-500/20" 
                  : "border-slate-300 focus:border-blue-600 focus:ring-blue-600/20"
              }`}
              id="categoryId"
              aria-describedby={error ? "add-budget-error" : undefined}
              ref={firstFieldRef}
              name="categoryId"
              value={form.categoryId}
              onChange={onChange}
              disabled={loadingCats || visibleCategories.length === 0 || catsError}
              aria-invalid={categoryMissing || undefined}
              required
            >
              <option value="">
                {visibleCategories.length === 0 ? "No expense categories" : "Select category"}
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
              className="h-9 rounded-md border border-slate-300 bg-white px-3 text-sm font-semibold text-slate-900 shadow-sm transition hover:bg-slate-100"
              aria-label="Create new category"
            >
              + New
            </button>
          </div>

          {/* value (required) */}
          <label htmlFor="value" className="sr-only">Value</label>
          <input
            className={`h-9 rounded-md border px-3 text-slate-900 shadow-sm outline-none transition focus:ring-2 ${
              valueInvalid
                ? "border-red-500 focus:ring-red-500/20"
                : "border-slate-300 focus:border-blue-600 focus:ring-blue-600/20"
            }`}
            id="value"
            aria-describedby={error ? "add-budget-error" : undefined}
            aria-invalid={valueInvalid || undefined}
            type="number"
            inputMode="decimal"
            min="0"
            step="0.01"
            name="value"
            placeholder="Value"
            value={form.value}
            onChange={onChange}
            autoComplete="off"
            required
          />

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
              disabled={submitting || valueInvalid || categoryMissing || loadingCats}
              className="h-9 rounded-md bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:opacity-60"
            >
              {submitting ? "Saving..." : "Save"}
            </button>
          </div>
        </form>
      </div>

      {/* Child modal */}
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
        initialType="EXPENSE"
      />
    </div>
  );
}

export default AddBudgetModal;