import { useState, useEffect, useId } from "react";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";
import api from "../api/api";

function CreateBudgetPage() {
  const { token, loading } = useAuth();
  const navigate = useNavigate();

  const monthId = useId();
  const valueId = useId();
  const categoryId = useId();

   const defaultMonth = new Date().toISOString().slice(0, 7);
  
  const [form, setForm] = useState(() => ({
    value: "",
    month: defaultMonth,
    categoryId: "",
  }));
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loadingCats, setLoadingCats] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (loading) return;
    if (!token) {
      navigate("/");
      return;
    }

    let cancelled = false;
    setLoadingCats(true);
    (async () => {
      try {
        const res = await api.get("/categories", {
          headers: { Authorization: `Bearer ${token}`},
        });
        if (!cancelled) setCategories(res.data);
      } catch (err) {
        if (!cancelled) {
          console.error(err);
          setError(err.response?.data?.message || "Error fetching categories");
        }
      } finally {
        if (!cancelled) setLoadingCats(false);
      }
    })();

    return () => { cancelled = true; };
  }, [loading, token, navigate]);

  const handleChange = (e) => {
    setError("");
    setSuccess("");
    const { name, value } = e.target;
    setForm((f) => ({...f, [name] : value }));
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    const valueNum = parseFloat(form.value);
    const categoryIdNum = parseInt(form.categoryId, 10);
      
    if (Number.isNaN(valueNum) || Number.isNaN(categoryIdNum)) {
      setError("Please enter a valid amount and select a category");
      return;
    }

    setSubmitting(true);
    try {
      const payload = { month: form.month, value: valueNum, categoryId: categoryIdNum };

      await api.post("/budgets", payload, {
        headers: { Authorization: `Bearer ${token}`},
      });

      setSuccess("Budget created")
      setForm({ value: "", month: defaultMonth, categoryId: ""}); // Reset form
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || "Error creating budget");
    } finally {
      setSubmitting(false);
    }
  };

  const formInvalid = !form.value || !form.month || !form.categoryId;

  return (
    <div style={{ maxWidth: 600, margin: "2rem auto"}}>
      <h2>Create Budget</h2>

      {error && <p role="alert" aria-live="polite" style={{ color: "red"}}>{error}</p>}
      {success && <p aria-live="polite" style={{ color: "green"}}>{success}</p>}

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor={valueId}>Value: </label><br />
          <input
            id={valueId}
            type="number"
            name="value"
            min="0"
            step="0.01"
            value={form.value}
            onChange={handleChange}
            required
            inputMode="decimal"
          />
        </div>

        <div style={{ marginTop: "1rem"}}>
          <label htmlFor={monthId}>Month:</label><br />
          <input
            id={monthId}
            type="month"
            name="month"
            value={form.month}
            onChange={handleChange}
            required
          />
        </div>

        <div style={{ marginTop: "1rem"}}>
          <label htmlFor={categoryId}>Category:</label><br />
          <select
            id={categoryId}
            name="categoryId"
            value={form.categoryId}
            onChange={handleChange}
            required
            disabled={loadingCats}
          >
            <option value="">{loadingCats ? "Loading..." : "Select a category"}</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <button 
          type="submit" 
          disabled={submitting || loadingCats || formInvalid} 
          style={{ marginTop: "1.5rem"}}
        >
          {submitting ? "Submitting..." : "Submit"}
        </button>
      </form>
    </div>
  );
}

export default CreateBudgetPage;