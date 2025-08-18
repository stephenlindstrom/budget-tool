import { useEffect, useRef, useState } from "react";
import PropTypes from "prop-types";

/**
 * Accessible modal to create a new category.
 *
 * Props:
 * - isOpen: boolean
 * - onClose: () => void
 * - onCreate: async (name: string, type: "INCOME"|"EXPENSE") => Promise<{ id: number|string, name: string, type: string }>
 * - categories: [{ id, name, type }] // used for client-side duplicate check
 * - initialName: string
 * - initialType: "INCOME" | "EXPENSE" | ""
 */
export default function CategoryModal({
  isOpen,
  onClose,
  onCreate,
  categories = [],
  initialName = "",
  initialType = "",
}) {
  const dialogRef = useRef(null);
  const inputRef = useRef(null);
  const lastFocusedRef = useRef(null);

  const [name, setName] = useState(initialName);
  const [type, setType] = useState(initialType);
  const [error, setError] = useState("");
  const [creating, setCreating] = useState(false);

  // Reset fields when opening
  useEffect(() => {
    if (isOpen) {
      setName(initialName);
      setType(initialType);
      setError("");
    }
  }, [isOpen, initialName, initialType]);

  // Basic focus mgmt + return focus on close
  useEffect(() => {
    if (!isOpen) return;
    lastFocusedRef.current = document.activeElement;
    const id = setTimeout(() => inputRef.current?.focus(), 0);
    return () => {
      clearTimeout(id);
      lastFocusedRef.current?.focus?.();
    };
  }, [isOpen]);

  // Lightweight focus trap + Esc handling
  useEffect(() => {
    if (!isOpen) return;

    function handleKeyDown(e) {
      if (e.key === "Escape") {
        e.stopPropagation();
        if (!creating) onClose();
        return;
      }
      if (e.key !== "Tab") return;

      const focusable = dialogRef.current?.querySelectorAll(
        'a[href], button:not([disabled]), textarea, input, select, [tabindex]:not([tabindex="-1"])'
      );
      if (!focusable || focusable.length === 0) return;

      const first = focusable[0];
      const last = focusable[focusable.length - 1];

      if (e.shiftKey) {
        if (document.activeElement === first) {
          e.preventDefault();
          last.focus();
        }
      } else {
        if (document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    }

    document.addEventListener("keydown", handleKeyDown, true);
    return () => document.removeEventListener("keydown", handleKeyDown, true);
  }, [isOpen, creating, onClose]);

  // Backdrop click closes (but not clicks inside dialog)
  const onBackdropClick = (e) => {
    if (e.target === e.currentTarget && !creating) onClose();
  };

  const validate = () => {
    const trimmed = name.trim();
    if (!trimmed) return "Please enter a category name.";
    if (trimmed.length > 50) return "Name must be 50 characters or less.";
    if (!type) return "Please select a type.";
    const dupe = categories.some(
      (c) =>
        c.name?.trim().toLowerCase() === trimmed.toLowerCase() &&
        (c.type || "").toLowerCase() === type.toLowerCase()
    );
    if (dupe) return "That category already exists for this type.";
    return "";
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (creating) return;

    const msg = validate();
    if (msg) {
      setError(msg);
      if (!name.trim()) inputRef.current?.focus();
      else if (!type) document.getElementById("new-cat-type")?.focus();
      return;
    }

    try {
      setCreating(true);
      setError("");
      await onCreate(name.trim(), type);
      onClose();
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.message ||
        "Error creating category.";
      setError(msg);
    } finally {
      setCreating(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div
      onClick={onBackdropClick}
      className="fixed inset-0 z-50 grid place-items-center bg-black/40"
    >
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="new-cat-title"
        aria-describedby="new-cat-desc"
        onClick={(e) => e.stopPropagation()}
        className="w-[calc(100%-2rem)] max-w-sm rounded-xl bg-white p-5 shadow-lg"
      >
        <h3 id="new-cat-title" className="mb-1 text-lg font-semibold text-slate-900">
          Create Category
        </h3>
        <p id="new-cat-desc" className="mb-3 text-sm text-slate-600">
          Add a new category.
        </p>

        {error && (
          <p
            role="alert"
            className="mb-3 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700"
          >
            <strong className="font-semibold">Error:</strong> {error}
          </p>
        )}

        <form onSubmit={handleSubmit} className="grid gap-3">
          <div>
            <label
              htmlFor="new-cat-name"
              className="mb-1 block text-sm font-medium text-slate-800"
            >
              Name
            </label>
            <input
              id="new-cat-name"
              ref={inputRef}
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={creating}
              maxLength={60}
              required
              className="h-9 w-full rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20 disabled:bg-slate-50 disabled:text-slate-500"
            />
            <p className="mt-1 text-xs text-slate-500">Max 50 characters.</p>
          </div>

          <div>
            <label
              htmlFor="new-cat-type"
              className="mb-1 block text-sm font-medium text-slate-800"
            >
              Type
            </label>
            <select
              id="new-cat-type"
              value={type}
              onChange={(e) => setType(e.target.value)}
              required
              disabled={creating}
              className="h-9 w-full rounded-md border border-slate-300 bg-white px-3 text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20 disabled:bg-slate-50 disabled:text-slate-500"
            >
              <option value="">Select a type</option>
              <option value="INCOME">Income</option>
              <option value="EXPENSE">Expense</option>
            </select>
          </div>

          <div className="mt-1 flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              disabled={creating}
              className="h-9 rounded-md border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-900 shadow-sm transition hover:bg-slate-100 disabled:opacity-60"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={creating || !name.trim() || !type}
              className="h-9 rounded-md bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:opacity-60"
            >
              {creating ? "Creating..." : "Create"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

CategoryModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onCreate: PropTypes.func.isRequired,
  categories: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
      name: PropTypes.string,
      type: PropTypes.string,
    })
  ),
  initialName: PropTypes.string,
  initialType: PropTypes.string,
};