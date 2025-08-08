import { useId, useMemo, useRef, useState } from "react";
import CategorySelect from "./CategorySelect";

export default function BudgetForm({
  categories,
  loadingCats = false,
  defaultMonth,
  onSubmit, 
  onAddCategoryClick,
  resetAfterSubmit = true,
}) {
  const valueId = useId();
  const monthId = useId();
  const categoryId = useId();
  const successRef = useRef(null);
  const valueRef = useRef(null);
  const categoryRef = useRef(null);

  // Live messages
  const [formError, setFormError] = useState("");
  const [formSuccess, setFormSuccess] = useState("");
  const [submitting, setSubmitting] = useState(false);

  // Form state
  const [form, setForm] = useState(() => ({
    value: "",
    month: defaultMonth ?? new Date().toISOString().slice(0, 7),
    categoryId: "",
  }));

  // Simple per-field errors (for aria-describedby)
  const [fieldErrors, setFieldErrors] = useState({ value: "", categoryId: "" });

  const formInvalid = useMemo(() => {
    return (
      form.value === "" ||
      !Number.isFinite(form.value) ||
      !form.month ||
      form.categoryId === ""
    );
  }, [form]);

  const handleChange = (e) => {
    setFormError("");
    setFormSuccess("");

    const { name, value, valueAsNumber, type } = e.target;

    let next;
    if (type === "number" || name === "value") {
      next = value === "" ? "" : valueAsNumber; // number or ""
    } else if (name === "categoryId") {
      next = value === "" ? "" : Number.parseInt(value, 10); // int or ""
    } else {
      next = value; // month stays YYYY-MM
    }

    setForm((f) => ({...f, [name]: next}));

    // Clear inline error for the field being edited
    setFieldErrors((fe) => ({...fe, [name]: ""}));
  };

  const validate = () => {
    const errors = { value: "", categoryId: ""};
    if (form.value === "" || !Number.isFinite(form.value)) {
      errors.value = "Please enter a valid amount.";
    } else if (form.value < 0) {
      errors.value = "Amount cannot be negative.";
    }
    if (form.categoryId === "") {
      errors.categoryId = "Please select a category.";
    }
    setFieldErrors(errors);
    // Return first invalid field name (or null)
    if (errors.value) return "value";
    if (errors.categoryId) return "categoryId";
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError("");
    setFormSuccess("");

    const firstInvalid = validate();
    if (firstInvalid) {
      // Move focus to first invalid field
      if (firstInvalid === "value") valueRef.current?.focus();
      if (firstInvalid === "categoryId") categoryRef.current?.focus();
      setFormError("Please fix the errors and try again.");
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        month: form.month,
        value: form.value,
        categoryId: form.categoryId,
      };
      await onSubmit?.(payload);

      setFormSuccess("Budget created");
      // Move focus to success for SR; requires tabIndex below
      setTimeout(() => successRef.current?.focus(), 0);

      if (resetAfterSubmit) {
        setForm((f) => ({
          value: "",
          month: f.month, // keep same month for rapid entry
          categoryId: "",
        }));
      }
    } catch (err) {
      // Parent should throw or rethrow server errors; surface message here
      const msg =
        err?.response?.data?.message ||
        err?.message ||
        "Error creating budget";
      setFormError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const valueErrId = `${valueId}-err`;
  const categoryErrId = `${categoryId}-err`;

  return (
    <form onSubmit={handleSubmit} aria-busy={submitting || undefined}>
      {/* Global status messages */}
      {formError && (
        <p role="alert" style={{ color: "red" }}>
          <strong>Error:</strong> {formError}
        </p>
      )}
      {formSuccess && (
        <p
          ref={successRef}
          tabIndex={-1}
          role="status"
          aria-live="polite"
          style={{ color: "green"}}
        >
          <strong>Success:</strong> {formSuccess}
        </p>
      )}

      {/* Value */}
      <div>
        <label htmlFor={valueId}>Value</label>
        <br />
        <input
          id={valueId}
          ref={valueRef}
          type="number"
          name="value"
          min="0"
          step="0.01"
          inputMode="decimal"
          required
          value={form.value}
          onChange={handleChange}
          aria-invalid={Boolean(fieldErrors.value)}
          aria-describedby={fieldErrors.value ? valueErrId : undefined}
        />
        {fieldErrors.value && (
          <div id={valueErrId} style={{ color: "red" }}>
            {fieldErrors.value}
          </div>
        )}
      </div>

      {/* Month */}
      <div style={{ marginTop: "1rem" }}>
        <label htmlFor={monthId}>Month</label>
        <br />
        <input
          id={monthId}
          type="month"
          name="month"
          required
          value={form.month}
          onChange={handleChange}
        />
      </div>

      {/* Category */}
      <div style={{ marginTop: "1rem" }}>
        <label htmlFor={categoryId}>Category</label>
        <br />
        <CategorySelect
          id={categoryId}
          name="categoryId"
          value={form.categoryId}
          categories={categories}
          loading={loadingCats}
          onChange={handleChange}
          onAddCategoryClick={onAddCategoryClick}
          selectRef={categoryRef}
          aria-invalid={Boolean(fieldErrors.categoryId)}
          aria-describedby={fieldErrors.categoryId ? categoryErrId : undefined}
        />
        {fieldErrors.categoryId && (
          <div id={categoryErrId} style={{ color: "red" }}>
            {fieldErrors.categoryId}
          </div>
        )}
      </div>

      <button
        type="submit"
        disabled={submitting || loadingCats || formInvalid}
        style={{ marginTop: "1.5rem" }}
      >
        {submitting ? "Submitting..." : "Submit"}
      </button>
    </form>
  );
}