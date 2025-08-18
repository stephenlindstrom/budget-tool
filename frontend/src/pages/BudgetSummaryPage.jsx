import { useState, useEffect, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/api";

const fmtUSD = new Intl.NumberFormat(undefined, { style: "currency", currency: "USD" });

function BudgetSummaryPage() {
  const { value } = useParams();
  const navigate = useNavigate();

  const [data, setData] = useState(null); // { monthDTO, budgetSummaryDTOs }
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const month = useMemo(() => (/^\d{4}-\d{2}$/.test(value || "") ? value : null), [value]);
  const monthLabel = data?.monthDTO?.display ?? month ?? value;

  useEffect(() => {
    if (!month) {
      setError("Invalid month format. Expected YYYY-MM.");
      return;
    }

    const controller = new AbortController();

    (async () => {
      setLoading(true);
      setError("");
      try {
        const res = await api.get(`/budgets/summary/${month}`, { signal: controller.signal });
        setData(res.data ?? null);
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
  }, [month, navigate]);

  const summaries = useMemo(() => data?.budgetSummaryDTOs ?? [], [data?.budgetSummaryDTOs]);

  const totals = useMemo(() => {
    return summaries.reduce(
      (acc, s) => {
        const budgeted = Number(s.budgeted ?? 0);
        const spent = Number(s.spent ?? 0);
        acc.budgeted += budgeted;
        acc.spent += spent;
        return acc;
      },
      { budgeted: 0, spent: 0 }
    );
  }, [summaries]);

  const remainingTotal = totals.budgeted - totals.spent;

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-8">
      <div className="mb-4">
        <button
          onClick={() => navigate("/dashboard")}
          className="h-9 rounded-md border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-900 shadow-sm transition hover:bg-slate-100 focus:outline-none focus:ring-2 focus:ring-blue-600/20"
        >
          ← Back to Dashboard
        </button>
      </div>

      <header className="mb-6">
        <h2 className="text-2xl font-semibold text-slate-900">Budgets for {monthLabel}</h2>
        <p className="mt-1 text-sm text-slate-600">
          Overview of your planned vs. actual spending by category.
        </p>
      </header>

      {error && (
        <div
          role="alert"
          className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
        >
          <strong className="font-semibold">Error:</strong> {error}
        </div>
      )}

      {loading && (
        <>
          {/* Summary skeleton */}
          <div className="mb-4 flex flex-wrap gap-2">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="h-8 w-40 animate-pulse rounded-full bg-slate-200" />
            ))}
          </div>
          {/* List skeleton */}
          <ul className="grid gap-2">
            {Array.from({ length: 6 }).map((_, i) => (
              <li key={i} className="h-14 animate-pulse rounded-lg border border-slate-200 bg-slate-100" />
            ))}
          </ul>
        </>
      )}

      {!loading && !error && summaries.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-8 text-center shadow-sm">
          <h3 className="text-lg font-semibold text-slate-900">No budgets found</h3>
          <p className="mt-2 text-sm text-slate-600">
            There are no budgets for {monthLabel}. Create one to see it here.
          </p>
        </div>
      )}

      {!loading && summaries.length > 0 && (
        <>
          {/* Summary chips */}
          <div className="mb-4 flex flex-wrap items-center gap-2">
            <span className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-sm text-slate-700">
              <strong className="font-semibold">Total Budgeted:</strong>{" "}
              {fmtUSD.format(totals.budgeted)}
            </span>
            <span className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-sm text-slate-700">
              <strong className="font-semibold">Total Spent:</strong>{" "}
              {fmtUSD.format(totals.spent)}
            </span>
            <span
              className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-sm ${
                remainingTotal < 0
                  ? "border border-red-200 bg-red-50 text-red-700"
                  : "border border-emerald-200 bg-emerald-50 text-emerald-700"
              }`}
            >
              <strong className="font-semibold">Remaining:</strong>{" "}
              {fmtUSD.format(remainingTotal)}
            </span>
          </div>

          {/* Category list */}
          <dl className="grid gap-2">
            {summaries
              .slice()
              .sort(
                (a, b) =>
                  a?.category?.name?.localeCompare(b?.category?.name || "") || 0
              )
              .map(({ id, category, budgeted, spent, remaining }) => {
                const b = Number(budgeted ?? 0);
                const s = Number(spent ?? 0);
                const r = Number(remaining ?? b - s);
                const negative = r < 0;

                return (
                  <div
                    key={id}
                    className="rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm transition hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md"
                  >
                    <dt className="mb-1 text-sm font-semibold text-slate-900">
                      {category?.name ?? "Uncategorized"}
                    </dt>
                    <dd className="m-0 text-sm text-slate-700">
                      <span className="mr-4">
                        <span className="font-medium text-slate-800">Budgeted:</span>{" "}
                        {fmtUSD.format(b)}
                      </span>
                      <span className="mr-4">
                        <span className="font-medium text-slate-800">Spent:</span>{" "}
                        {fmtUSD.format(s)}
                      </span>
                      <span
                        className={`${
                          negative ? "text-red-700" : "text-emerald-700"
                        }`}
                      >
                        <span className="font-medium">
                          Remaining:
                        </span>{" "}
                        {fmtUSD.format(r)}
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