import { useState, useEffect, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/api";
import { useAuth } from "../hooks/useAuth";

const fmtUSD = new Intl.NumberFormat(undefined, { style: "currency", currency: "USD" });

function BudgetSummaryPage() {
  const { value } = useParams();
  const { token } = useAuth();
  const navigate = useNavigate();

  const [budgets, setBudgets] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const month = useMemo(() => (/^\d{4}-\d{2}$/.test(value || "") ? value : null), [value]);

  useEffect(() => {
    if (!token) {
      navigate("/");
      return;
    }
    if (!month) {
      setError("Invalid month format. Expected YYYY-MM.");
      return;
    }

    const controller = new AbortController();
    (async () => {
      setLoading(true);
      setError("");
      try {
        const res = await api.get(`/budgets/summary/${month}`, {
          signal: controller.signal,
          headers: { Authorization: `Bearer ${token}`},
        });
        setBudgets(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        if (controller.signal.aborted) return;
        console.error(err);
        const msg = err?.response?.data?.message ?? `Failed to load budgets for ${month}`;
        setError(msg);
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    })();

    return () => controller.abort();

  }, [token, month, navigate]);

  const totals = useMemo(() => {
    return budgets.reduce(
      (acc, b) => {
        const budgeted = Number(b.budgeted ?? 0);
        const spent = Number(b.spent ?? 0);
        acc.budgeted += budgeted;
        acc.spent += spent;
        return acc;
      },
      { budgeted: 0, spent: 0 }
    );
  }, [budgets]);
  const remainingTotal = totals.budgeted - totals.spent;

  return (
    <div style={{ maxWidth: 640, margin: "2rem auto"}}>
      <button onClick={() => navigate("/dashboard")} style={{ marginBottom: "1rem" }}>
        Back to Dashboard
      </button>

      <h2>Budgets for { month ?? value }</h2>

      {error && <p role="alert" style={{ color: "red"}}><strong>Error:</strong> {error}</p>}

      {loading ? (
        <p aria-live="polite">Loading budgets...</p>
      ) : budgets.length === 0 ? (
        <p>No budgets found for this month.</p>
      ) : (
        <>
          {/* Summary */}
          <div style={{ margin: "0 0 1rem 0" }}>
            <strong>Total Budgeted:</strong> {fmtUSD.format(totals.budgeted)}{" "}
            <strong>Total Spent:</strong> {fmtUSD.format(totals.spent)}{" "}
            <strong>Remaining:</strong>{" "}
            <span style={{ color: remainingTotal < 0 ? "crimson" : "inherit" }}>
              {fmtUSD.format(remainingTotal)}
            </span>
          </div>

          {/* List */}
          <dl>
            {budgets
              .slice()
              .sort((a, b) => a?.category?.name?.localeCompare(b?.category?.name || "") || 0)
              .map(({ id, category, budgeted, spent, remaining }) => {
                const b = Number(budgeted ?? 0);
                const s = Number(spent ?? 0);
                const r = Number(remaining ?? b - s);
                return (
                  <div key={id} style={{ padding: "0.5rem 0", borderBottom: "1px solid #eee" }}>
                    <dt><strong>{category?.name ?? "Uncategorized"}</strong></dt>
                    <dd style={{ margin: 0 }}>
                      Budgeted: {fmtUSD.format(b)} Spent: {fmtUSD.format(s)}{" "}
                      <span style={{ color: r < 0 ? "crimson" : "inherit" }}>
                        Remaining: {fmtUSD.format(r)}
                      </span>
                    </dd>
                  </div>
                );
              })}
          </dl>
        </>
      )}
    </div>  
  );
}

export default BudgetSummaryPage;