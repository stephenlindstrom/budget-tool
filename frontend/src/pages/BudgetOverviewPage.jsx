import { useEffect, useMemo, useState } from "react";
import api from "../api/api";
import { DollarSign, TrendingDown, Wallet, PiggyBank, Plus } from "lucide-react";
import { Link } from "react-router-dom";
import AddBudgetModal from "../components/budgets/AddBudgetModal";

export default function BudgetOverviewPage() {
  const [rows, setRows] = useState([]);           // Array<MonthlyOverviewDTO>
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedMonth, setSelectedMonth] = useState(null); // "YYYY-MM"

  const [openAddBudget, setOpenAddBudget] = useState(false);

  const currency = useMemo(
    () =>
      new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
        minimumFractionDigits: 2,
      }),
    []
  );

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError("");
      try {
        const res = await api.get("/budgets/overview");
        const list = Array.isArray(res?.data) ? res.data : [];
        // Normalize & sort newest-first just in case
        const normalized = list
          .map((r) => ({
            month: toYm(r?.month), // "YYYY-MM"
            budgeted: toNum(r?.budgeted),
            spent: toNum(r?.spent),
            remaining: toNum(r?.remaining),
            income: toNum(r?.income),
          }))
          .sort((a, b) => (a.month < b.month ? 1 : a.month > b.month ? -1 : 0));

        if (!cancelled) {
          setRows(normalized);
          setSelectedMonth((prev) => prev || normalized[0]?.month || toYearMonth(new Date()));
        }
      } catch (e) {
        console.error(e);
        if (!cancelled) setError("Failed to load monthly overview.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, []);

  const selected = useMemo(
    () => rows.find((r) => r.month === selectedMonth) ?? {
      month: selectedMonth || toYearMonth(new Date()),
      budgeted: 0, spent: 0, remaining: 0, income: 0,
    },
    [rows, selectedMonth]
  );

  const usagePct = selected.budgeted > 0
    ? Math.max(0, Math.min(100, Math.round((selected.spent / selected.budgeted) * 100)))
    : 0;

  return (
    <div className="mx-auto max-w-6xl p-4">
      {/* Header */}
      <div className="mb-5 grid grid-cols-1 items-center gap-3 sm:grid-cols-[1fr_auto]">
        <h1 className="text-xl font-semibold">Budget Overview</h1>

        <div className="flex items-center gap-2 justify-end">
          <div className="flex items-center gap-2">
            <label htmlFor="month" className="text-sm text-slate-600 cursor-pointer">Month</label>
            <input
              id="month"
              type="month"
              onClick={(e) => e.currentTarget.showPicker?.()}
              value={selectedMonth || ""}
              onChange={(e) => setSelectedMonth(e.target.value)}
              className="rounded-lg border px-2 py-1 text-sm cursor-pointer focus:cursor-text disabled:cursor-not-allowed" 
            />
          </div>
          <button
            type="button"
            onClick={() => setOpenAddBudget(true)}
            className="h-9 w-full sm:w-auto inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 cursor-pointer"
          >
            <Plus size={16} />
            Add budget
          </button>
        </div>
      </div>

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </div>
      )}

      {/* KPI Tiles */}
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard
          title="Total Budgeted"
          value={currency.format(selected.budgeted)}
          icon={<Wallet className="h-4 w-4" />}
        />
        <KpiCard
          title="Total Spent"
          value={currency.format(selected.spent)}
          icon={<TrendingDown className="h-4 w-4" />}
        />
        <KpiCard
          title="Total Remaining"
          value={currency.format(selected.remaining)}
          emphasis={selected.remaining < 0 ? "danger" : "ok"}
          icon={<PiggyBank className="h-4 w-4" />}
        />
        <KpiCard
          title="Total Income"
          value={currency.format(selected.income ?? 0)}
          icon={<DollarSign className="h-4 w-4" />}
        />
      </div>

      {/* Usage bar */}
      <div className="mt-4">
        <div className="mb-1 flex items-center justify-between text-xs text-slate-600">
          <span>Budget used in {fmtMonth(selected.month)}</span>
          <span>{usagePct}%</span>
        </div>
        <div className="h-2 w-full rounded-full bg-slate-200">
          <div
            className={`h-2 rounded-full ${selected.remaining < 0 ? "bg-red-500" : "bg-emerald-500"}`}
            style={{ width: `${usagePct}%` }}
          />
        </div>
      </div>

      {/* Table */}
      <div className="mt-6 overflow-x-auto rounded-2xl border bg-white shadow-sm">
        <table className="min-w-[640px] w-full border-separate border-spacing-0">
          <thead>
            <tr className="text-left">
              {["Month", "Budgeted", "Spent", "Remaining", "Income", "Used %", "Details"].map((h) => (
                <th key={h} className="border-b bg-slate-50 px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-600">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading
              ? Array.from({ length: 6 }).map((_, i) => (
                  <tr key={i} className="border-b last:border-0">
                    {Array.from({ length: 6 }).map((__, j) => (
                      <td key={j} className="px-3 py-3">
                        <div className="h-4 w-24 animate-pulse rounded bg-slate-200" />
                      </td>
                    ))}
                  </tr>
                ))
              : rows.map((r) => {
                  const used = r.budgeted > 0 ? Math.round((r.spent / r.budgeted) * 100) : 0;
                  const isActive = r.month === selected.month;
                  return (
                    <tr
                      key={r.month}
                      onClick={() => setSelectedMonth(r.month)}
                      className={`cursor-pointer border-b last:border-0 hover:bg-slate-50 ${
                        isActive ? "bg-slate-50/70" : ""
                      }`}
                      title="Click to view this month above"
                    >
                      <td className="px-3 py-3 font-medium">{fmtMonth(r.month)}</td>
                      <td className="px-3 py-3 tabular-nums">{currency.format(r.budgeted)}</td>
                      <td className="px-3 py-3 tabular-nums">{currency.format(r.spent)}</td>
                      <td className={`px-3 py-3 tabular-nums ${r.remaining < 0 ? "text-red-600" : ""}`}>
                        {currency.format(r.remaining)}
                      </td>
                      <td className="px-3 py-3 tabular-nums">{currency.format(r.income ?? 0)}</td>
                      <td className="px-3 py-3 tabular-nums">{Math.max(0, Math.min(100, used))}%</td>
                      <td className="px-3 py-3">
                        <Link
                          to={`/budgets/${r.month}`}
                          onClick={(e) => e.stopPropagation()} // prevents row onClick
                          className="inline-flex items-center gap-1 rounded-lg border px-2 py-1 text-sm text-slate-700 hover:bg-slate-50"
                          title="Open month details"
                        >
                          View
                        </Link>
                      </td>
                    </tr>
                  );
                })}
          </tbody>
        </table>
      </div>
      <AddBudgetModal
        open={openAddBudget}
        onClose={() => setOpenAddBudget(false)}
        defaultMonth={selectedMonth || toYearMonth(new Date())}
        onCreated={(b) => {
          const ym = toYm(b?.month);
          const val = toNum(b?.value);
          setRows((prev) => upsertOverview(prev, ym, val));
          setSelectedMonth(ym); // jump to the month that was just edited
        }}
      />
    </div>
  );
}

/* ---------- small presentational pieces ---------- */

function KpiCard({ title, value, icon, emphasis }) {
  const ring =
    emphasis === "danger" ? "ring-red-200 bg-red-50" :
    emphasis === "ok" ? "ring-emerald-200 bg-emerald-50" :
    "ring-slate-200 bg-white";
  return (
    <div className={`flex items-center justify-between rounded-2xl border p-4 shadow-sm ring-1 ${ring}`}>
      <div>
        <div className="text-sm text-slate-500">{title}</div>
        <div className="mt-1 text-lg font-semibold tabular-nums">{value}</div>
      </div>
      <div className="rounded-xl border bg-white p-2 text-slate-600">{icon}</div>
    </div>
  );
}

/* ---------- helpers ---------- */

function toYearMonth(d) {
  const y = d.getFullYear();
  const m = `${d.getMonth() + 1}`.padStart(2, "0");
  return `${y}-${m}`;
}

function toYm(v) {
  if (!v) return null;
  if (typeof v === "string") return v.length === 7 ? v : String(v).slice(0, 7);
  if (typeof v === "object" && v.year && v.month) {
    const mm = String(v.month).padStart(2, "0");
    return `${v.year}-${mm}`;
  }
  return String(v);
}

function toNum(v) {
  if (typeof v === "number") return v;
  if (v == null) return 0;
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

function fmtMonth(ym) {
  if (!ym || typeof ym !== "string" || ym.length < 7) return ym || "";
  const [y, m] = ym.split("-").map(Number);
  const d = new Date(y, (m || 1) - 1, 1);
  return d.toLocaleDateString("en-US", { month: "short", year: "numeric" }); // e.g., "Aug 2025"
}

function round2(n) {
  return Math.round((Number(n) + Number.EPSILON) * 100) / 100;
}

function upsertOverview(list, ym, addValue) {
  let found = false;
  const next = list.map((r) => {
    if (r.month !== ym) return r;
    found = true;
    const budgeted = round2(r.budgeted + addValue);
    const remaining = round2(budgeted - r.spent);
    return { ...r, budgeted, remaining };
  });
  if (!found) {
    next.push({
      month: ym,
      budgeted: round2(addValue),
      spent: 0,
      remaining: round2(addValue),
      income: 0,
    });
  }

  next.sort((a, b) => (a.month < b.month ? 1 : a.month > b.month ? -1 : 0));
  return next;
}
