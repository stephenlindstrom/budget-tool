import { useEffect } from "react";

export default function ConfirmDialog({
  open,
  title = "Are you sure?",
  body = "This action cannot be undone.",
  confirmText = "Delete",
  cancelText = "Cancel",
  destructive = false,
  loading = false,
  onConfirm,
  onCancel,
}) {
  useEffect(() => {
    if (!open) return;
    const onKey = (e) => {
      if (e.key === "Escape") onCancel?.();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onCancel]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onCancel?.();
      }}
      aria-modal="true"
      role="dialog"
      aria-labelledby="confirm-title"
    >
      <div className="w-full max-w-sm rounded-xl bg-white p-5 shadow-lg">
        <h3 id="confirm-title" className="mb-2 text-lg font-semibold text-slate-900">
          {title}
        </h3>
        <p className="mb-4 text-sm text-slate-700">{body}</p>

        <div className="mt-2 flex justify-end gap-2">
          <button
            type="button"
            onClick={onCancel}
            disabled={loading}
            className="h-9 rounded-md border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-900 shadow-sm transition hover:bg-slate-100 disabled:opacity-60 cursor-pointer"
          >
            {cancelText}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={loading}
            className={`h-9 rounded-md px-4 text-sm font-semibold text-white shadow-sm transition disabled:opacity-60 cursor-pointer ${
              destructive ? "bg-red-600 hover:bg-red-700" : "bg-blue-600 hover:bg-blue-700"
            }`}
          >
            {loading ? "Working..." : confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}
