import { useState, useEffect } from "react";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";
import api from "../api/api";

function CreateBudgetPage() {
  const { token, loading } = useAuth();
  const navigate = useNavigate();
  
  const [form, setForm] = useState({value: "", month: "", categoryId: ""});
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Redirect to login if token is missing once auth has finished loading
  useEffect(() => {
    if (!loading && !token) {
      navigate("/login");
    }
  }, [loading, token, navigate]);

  if (loading) return null; // Don't render form while checking auth

  const handleChange = (e) => {
    setForm({...form, [e.target.name]: e.target.value});
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    try {
      await api.post("/budgets", form, {
        headers: { Authorization: `Bearer ${token}`},
      });
      setSuccess("Budget created")
      setForm({ value: "", month: "", categoryId: ""}); // Reset form
    } catch (err) {
      console.error(err);
      setError("Error creating budget");
    }
  };

  return (
    <div style={{ maxWidth: 600, margin: "2rem auto"}}>
      <h2>Create Budget</h2>
      {error && <p style={{ color: "red"}}>{error}</p>}
      {success && <p style={{ color: "green"}}>{success}</p>}
      <form onSubmit={handleSubmit}>
        <div>
          <label>Value: </label><br />
          <input
            type="number"
            name="value"
            value={form.value}
            onChange={handleChange}
            required
          />
        </div>
        <div style={{ marginTop: "1rem "}}>
          <label>Month:</label><br />
          <input
            type="month"
            name="month"
            value={form.month}
            onChange={handleChange}
            required
          />
        </div>
        <div style={{ marginTop: "1rem "}}>
          <label>Category ID:</label><br />
          <input
            type="number"
            name="categoryId"
            value={form.categoryId}
            onChange={handleChange}
            required
          />
        </div>
        <button type="submit" style={{ marginTop: "1.5rem"}}>Submit</button>
      </form>
    </div>
  );
}

export default CreateBudgetPage;