import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/api";

function DashboardPage() {
  const [months, setMonths] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchMonths = async () => {
      setLoading(true);
      setError("");
      try {
        const res = await api.get("/budgets/months");
        setMonths(res.data ?? []);
      } catch (err) {
        console.error(err);
        setError("Failed to load available months.");
      } finally {
        setLoading(false);
      }
    };

    fetchMonths();
  }, []);

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-8">
      <header className="mb-6">
        <h2 className="text-2xl font-semibold text-slate-900">Available Budget Months</h2>
        <p className="mt-1 text-sm text-slate-600">
          Select a month to view its budget summary and transactions.
        </p>
      </header>

      {error && (
        <div
          role="alert"
          className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
        >
          {error}
        </div>
      )}

      {/* Loading skeleton */}
      {loading && (
        <ul className="grid gap-3 sm:grid-cols-2">
          {Array.from({ length: 6 }).map((_, i) => (
            <li
              key={i}
              className="h-14 animate-pulse rounded-lg border border-slate-200 bg-slate-100"
            />
          ))}
        </ul>
      )}

      {/* Empty state */}
      {!loading && months.length === 0 && !error && (
        <div className="rounded-xl border border-slate-200 bg-white p-8 text-center shadow-sm">
          <h3 className="text-lg font-semibold text-slate-900">No budgets yet</h3>
          <p className="mt-2 text-sm text-slate-600">
            When you create your first budget, it will show up here.
          </p>
          {/* Optional: link to create a budget if you have a route */}
          {/* <button onClick={() => navigate('/budgets/new')} className="mt-4 h-9 rounded-md bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700">
            Create Budget
          </button> */}
        </div>
      )}

      {/* Months grid */}
      {!loading && months.length > 0 && (
        <ul className="grid gap-3 sm:grid-cols-2">
          {months.map(({ value, display }) => (
            <li key={value}>
              <button
                onClick={() => navigate(`/budgets/${value}`)}
                className="group flex w-full items-center justify-between rounded-lg border border-slate-200 bg-white px-4 py-3 text-left shadow-sm transition hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-600/20"
              >
                <span className="text-slate-900">{display}</span>
                <span className="ml-3 inline-flex items-center rounded-md border border-slate-200 bg-slate-50 px-2 py-0.5 text-xs font-medium text-slate-600 transition group-hover:border-blue-200 group-hover:text-blue-700">
                  View
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default DashboardPage;
