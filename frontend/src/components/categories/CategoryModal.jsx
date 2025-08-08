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

  // Reset field each time modal opens
  useEffect(() => {
    if (isOpen) {
      setName(initialName);
      setType(initialType);
      setError("");
    }
  }, [isOpen, initialName, initialType]);

  // Basic focus management + return focus on close
  useEffect(() => {
    if (!isOpen) return;
    lastFocusedRef.current = document.activeElement;
    // Delay focus to ensure element is mounted
    const id = setTimeout(() => inputRef.current?.focus(), 0);
    return () => {
      clearTimeout(id);
      // Return focus to the element that opened the modal
      lastFocusedRef.current?.focus?.();
    };
  }, [isOpen]);

  // Very lightweight focus trap for Tab / Shift+Tab
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

  // Backdrop click closes (but clicks inside dialog do not)
  const onBackdropClick = (e) => {
    if (e.target === e.currentTarget && !creating) onClose();
  };

  const validate = () => {
    const trimmed = name.trim();
    if (!trimmed) return "Please enter a category name.";
    if (trimmed.length > 50) return "Name must be 50 characters or less.";
    if (!type) return "Please select a type.";
    const dupe = categories.some(
      (c) => c.name?.trim().toLowerCase() === trimmed.toLowerCase() &&
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
      // let parent update categories and select it
      // Parent can close the modal immediately on success
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
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,0.4)",
        display: "grid",
        placeItems: "center",
        zIndex: 1000,
      }}
    >
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="new-cat-title"
        aria-describedby="new-cat-desc"
        style={{
          maxWidth: 400,
          width: "calc(100% - 2rem)",
          background: "white",
          borderRadius: 12,
          padding: 16,
          boxShadow: "0 10px 30px rgba(0,0,0,0.2)",
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <h3 id="new-cat-title" style={{ marginTop: 0 }}>
          Create Category
        </h3>
        <p id="new-cat-desc" style={{ marginTop: 0, color: "#555" }}>
          Add a new category for your budget.
        </p>

        {error && (
          <p role="alert" style={{ color: "red" }}>
            <strong>Error:</strong> {error}
          </p>
        )}

        <form onSubmit={handleSubmit}>
          <label htmlFor="new-cat-name">Name</label>
          <br />
          <input
            id="new-cat-name"
            ref={inputRef}
            value={name}
            onChange={(e) => setName(e.target.value)}
            disabled={creating}
            maxLength={60}
            style={{ width: "100%", marginTop: 4 }}
            required
          />

          <div>
            <label htmlFor="new-cat-type">Type</label>
            <br />
            <select
              id="new-cat-type"
              value={type}
              onChange={(e) => setType(e.target.value)}
              required
              disabled={creating}
              style={{ width: "100%", marginTop: 4 }}
            >
              <option value="">Select a type</option>
              <option value="INCOME">Income</option>
              <option value="EXPENSE">Expense</option>
            </select>
          </div>
          

          <div style={{ marginTop: 12, display: "flex", gap: 8, justifyContent: "flex-end" }}>
            <button type="button" onClick={onClose} disabled={creating}>
              Cancel
            </button>
            <button type="submit" disabled={creating || !name.trim() || !type}>
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
  initialName:PropTypes.string,
  initialType:PropTypes.string,
};
