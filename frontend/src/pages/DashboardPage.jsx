import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/api";
import { Plus, ExternalLink } from "lucide-react";

export default function DashboardPage() {
  const navigate = useNavigate();
  const [month, setMonth] = useState(toYearMonth(new Date()));
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // KPI source: /budgets/overview/{month}
  const [kpi, setKpi] = useState({ budgeted: 0, spent: 0, remaining: 0, income: 0 });

  // Category rows source: /budgets/summary/{month}
  const [rows, setRows] = useState([]); // { id, categoryName, type, budgeted, spent, remaining }

  const currency = useMemo(
    () => new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", minimumFractionDigits: 2 }),
    []
  );

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError("");

      // Fetch KPIs
      try {
        const r = await api.get(`/budgets/overview/${month}`);
        const d = r?.data ?? {};
        if (!cancelled) {
          setKpi({
            budgeted: toNum(d.budgeted),
            spent: toNum(d.spent),
            remaining: toNum(d.remaining),
            income: toNum(d.income),
          });
        }
      } catch (e) {
        console.error(e);
        if (!cancelled) setError("Failed to load monthly KPIs.");
      }

      // Fetch category rows
      try {
        const r = await api.get(`/budgets/summary/${month}`);
        const list = Array.isArray(r?.data?.budgetSummaryDTOs) ? r.data.budgetSummaryDTOs : [];
        if (!cancelled) {
          setRows(
            list.map((x) => ({
              id: x.id,
              categoryName: x?.category?.name ?? "(Uncategorized)",
              type: x?.category?.type ?? "EXPENSE",
              budgeted: toNum(x.budgeted),
              spent: toNum(x.spent),
              remaining: toNum(x.remaining),
            }))
          );
        }
      } catch (e) {
        console.error(e);
        if (!cancelled) setError((prev) => prev || "Failed to load category data.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => { cancelled = true; };
  }, [month]);

  const usagePct = useMemo(() => pct(kpi.spent, kpi.budgeted), [kpi.spent, kpi.budgeted]);
  const net = useMemo(() => kpi.income - kpi.spent, [kpi.income, kpi.spent]);

  // Compute “at-risk / overspent” (expense categories, highest % used)
  const atRisk = useMemo(() => {
    return rows
      .filter(r => r.type === "EXPENSE" && r.budgeted > 0)
      .map(r => ({ ...r, usedPct: pct(r.spent, r.budgeted) }))
      .sort((a, b) => b.usedPct - a.usedPct)
      .slice(0, 5);
  }, [rows]);

  // Compute “unbudgeted spend” (id === null, spent > 0)
  const unbudgeted = useMemo(() => {
    return rows
      .filter(r => r.type === "EXPENSE" && r.id == null && r.spent > 0)
      .sort((a, b) => b.spent - a.spent)
      .slice(0, 5);
  }, [rows]);

  return (
    <div className="mx-auto max-w-6xl p-4">
      {/* Header */}
      <div className="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-xl font-semibold">Dashboard</h1>
        <div className="flex items-center gap-2">
          <label htmlFor="month" className="text-sm text-slate-600">Month</label>
          <input
            id="month"
            type="month"
            value={month}
            onChange={(e) => setMonth(e.target.value)}
            className="rounded-lg border px-2 py-1 text-sm"
          />
          <button
            type="button"
            onClick={() => navigate(`/budgets/${month}`)}
            className="inline-flex items-center gap-1 rounded-lg border px-2 py-1 text-sm hover:bg-slate-50"
            title="Open full month details"
          >
            Details <ExternalLink className="h-3.5 w-3.5" />
          </button>
          <button
            type="button"
            onClick={() => navigate("/transactions")}
            className="inline-flex items-center gap-1 rounded-lg bg-emerald-600 px-3 py-1.5 text-sm text-white hover:bg-emerald-700"
            title="Add a transaction"
          >
            <Plus className="h-4 w-4" /> Add transaction
          </button>
        </div>
      </div>

      {error && (
        <div className="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
          {error}
        </div>
      )}

      {/* KPI tiles */}
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
        <Kpi title="Budgeted" value={currency.format(kpi.budgeted)} />
        <Kpi title="Spent" value={currency.format(kpi.spent)} />
        <Kpi
          title="Remaining"
          value={currency.format(kpi.remaining)}
          emphasis={kpi.remaining < 0 ? "danger" : "ok"}
        />
        <Kpi title="Income" value={currency.format(kpi.income)} />
        <Kpi title="Net (Income − Spent)" value={currency.format(net)} emphasis={net < 0 ? "danger" : "ok"} />
      </div>

      {/* Budget usage bar */}
      <div className="mt-4">
        <div className="mb-1 flex items-center justify-between text-xs text-slate-600">
          <span>Budget used</span>
          <span>{usagePct}%</span>
        </div>
        <div className="h-2 w-full rounded-full bg-slate-200">
          <div
            className={`h-2 rounded-full ${kpi.remaining < 0 ? "bg-red-500" : "bg-emerald-500"}`}
            style={{ width: `${usagePct}%` }}
          />
        </div>
      </div>

      {/* Two columns: At-Risk & Unbudgeted */}
      <div className="mt-6 grid gap-4 lg:grid-cols-2">
        <Card title="At-risk / overspent categories">
          {loading ? <SkeletonLines rows={5} /> :
            atRisk.length === 0 ? (
              <Empty text="No categories at risk. Nice work!" />
            ) : (
              <ul className="divide-y">
                {atRisk.map((r) => (
                  <li key={`${r.categoryName}-risk`} className="flex items-center justify-between gap-3 py-3">
                    <div className="min-w-0">
                      <div className="truncate font-medium">{r.categoryName}</div>
                      <div className="mt-1 flex items-center gap-2 text-xs text-slate-600">
                        <span>{currency.format(r.spent)} / {currency.format(r.budgeted)}</span>
                        <Bar value={r.usedPct} danger={r.remaining < 0} />
                      </div>
                    </div>
                    <div className={`tabular-nums text-sm ${r.remaining < 0 ? "text-red-600" : ""}`}>
                      {currency.format(r.remaining)}
                    </div>
                  </li>
                ))}
              </ul>
            )
          }
        </Card>

        <Card title="Unbudgeted spend (no budget set)">
          {loading ? <SkeletonLines rows={5} /> :
            unbudgeted.length === 0 ? (
              <Empty text="No unbudgeted spending this month." />
            ) : (
              <ul className="divide-y">
                {unbudgeted.map((r) => (
                  <li key={`${r.categoryName}-unbudgeted`} className="flex items-center justify-between gap-3 py-3">
                    <div className="min-w-0">
                      <div className="truncate font-medium">
                        {r.categoryName}
                        <span className="ml-2 rounded-full border border-amber-300 bg-amber-50 px-2 py-0.5 text-xs text-amber-700">
                          No budget
                        </span>
                      </div>
                      <div className="mt-1 text-xs text-slate-600">
                        Spent {currency.format(r.spent)}
                      </div>
                    </div>
                    <button
                      type="button"
                      className="rounded-lg border px-2 py-1 text-xs hover:bg-slate-50"
                      onClick={() => navigate(`/budgets/${month}`)}
                      title="Open month details to add a budget"
                    >
                      Review
                    </button>
                  </li>
                ))}
              </ul>
            )
          }
        </Card>
      </div>
    </div>
  );
}

/* ---------------- Presentational bits ---------------- */

function Kpi({ title, value, emphasis }) {
  const ring =
    emphasis === "danger" ? "ring-red-200 bg-red-50" :
    emphasis === "ok" ? "ring-emerald-200 bg-emerald-50" :
    "ring-slate-200 bg-white";
  return (
    <div className={`rounded-2xl border p-4 shadow-sm ring-1 ${ring}`}>
      <div className="text-sm text-slate-500">{title}</div>
      <div className="mt-1 text-lg font-semibold tabular-nums">{value}</div>
    </div>
  );
}

function Card({ title, children }) {
  return (
    <div className="rounded-2xl border bg-white shadow-sm">
      <div className="border-b px-4 py-3">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-600">{title}</h2>
      </div>
      <div className="px-4 py-3">{children}</div>
    </div>
  );
}

function Empty({ text }) {
  return <div className="py-6 text-sm text-slate-500">{text}</div>;
}

function SkeletonLines({ rows = 3 }) {
  return (
    <ul className="animate-pulse space-y-3 py-3">
      {Array.from({ length: rows }).map((_, i) => (
        <li key={i} className="h-4 rounded bg-slate-200" />
      ))}
    </ul>
  );
}

function Bar({ value, danger }) {
  const cl = danger ? "bg-red-500" : "bg-emerald-500";
  const w = Math.max(0, Math.min(100, Math.round(value)));
  return (
    <div className="h-1.5 w-24 rounded-full bg-slate-200">
      <div className={`h-1.5 rounded-full ${cl}`} style={{ width: `${w}%` }} />
    </div>
  );
}

/* ---------------- Helpers ---------------- */

function toYearMonth(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  return `${y}-${m}`;
}
function pct(part, total) {
  if (!total || total <= 0) return 0;
  const v = Math.round((part / total) * 100);
  return Math.max(0, Math.min(100, v));
}
function toNum(v) {
  if (typeof v === "number") return v;
  if (v == null || v === "") return 0;
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

