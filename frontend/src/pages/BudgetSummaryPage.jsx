import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/api";
import { useAuth } from "../hooks/useAuth";

function BudgetSummaryPage() {
  const { value } = useParams();
  const { token } = useAuth();
  const navigate = useNavigate();
  const [budgets, setBudgets] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!token) return;
    const fetchBudgets = async () => {
      setLoading(true);
      try {
        const res = await api.get(`/budgets/month/${value}`, {
          headers: { Authorization: `Bearer ${token}`},
        });
        setBudgets(res.data);
      } catch (err) {
        setError(`Failed to load budgets for ${value}`);
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchBudgets();
  }, [token, value]);

  return (
    <div style={{ maxWidth: 600, margin: "2rem auto"}}>
      <button onClick={() => navigate("/dashboard")} style={{ marginBottom: "1rem" }}>
        Back to Dashboard
      </button>

      <h2>Budgets for { value }</h2>
      {error && <p style={{ color: "red"}}>{error}</p>}
      {loading ? (
        <p>Loading budgets...</p>
      ) : budgets.length === 0 ? (
        <p>No budgets found for this month.</p>
      ) : (
        <ul>
          {budgets.map(({ id, category, value: amount }) => (
            <li key={id}>
              {category.name}: ${amount}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default BudgetSummaryPage;