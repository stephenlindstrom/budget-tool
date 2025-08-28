import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import api from "../api/api";
import { ArrowLeft, Search, Plus, Calculator } from "lucide-react";
import AddBudgetModal from "../components/budgets/AddBudgetModal";
import { useCategories } from "../hooks/useCategories";

export default function BudgetMonthPage() {
  const { month: monthParam } = useParams(); // route like /budgets/:month ("YYYY-MM")
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [monthValue, setMonthValue] = useState(monthParam || toYearMonth(new Date()));
  const [monthDisplay, setMonthDisplay] = useState("");
  const [items, setItems] = useState([]); // Array<BudgetSummaryDTO>
  const [incomeRows, setIncomeRows] = useState([]);
  const [incomeError, setIncomeError] = useState("");

  const [query, setQuery] = useState("");
  const [hideZero, setHideZero] = useState(false);
  const [sort, setSort] = useState({ key: "category", dir: "asc" });

  const [openAddBudget, setOpenAddBudget] = useState(false);

  const { categories, loading: loadingCats, error: catsError, refresh: refreshCats } = useCategories();

  const blockedCategoryIds = useMemo(
    () => new Set(items.filter(r => r.id != null).map(r => String(r.categoryId))),
    [items]
  );

  const currency = useMemo(
    () =>
      new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", minimumFractionDigits: 2 }),
    []
  );

  // fetch data for month
useEffect(() => {
  let cancelled = false;

  (async () => {
    setLoading(true);
    setError("");
    setIncomeError("");

    try {
      // Budgets/expenses for the month
      const res = await api.get(`/budgets/summary/${monthValue}`);
      const data = res?.data ?? {};
      const monthDTO = data.monthDTO ?? {};
      const budgetList = Array.isArray(data.budgetSummaryDTOs) ? data.budgetSummaryDTOs : [];

      if (!cancelled) {
        setMonthDisplay(monthDTO.display || fmtMonth(monthValue));
        setItems(
          budgetList.map((r) => ({
            id: r.id,
            categoryId: r?.category?.id ?? null,
            categoryName: r?.category?.name ?? "(Uncategorized)",
            type: r?.category?.type ?? "EXPENSE",
            budgeted: toNum(r.budgeted),
            spent: toNum(r.spent),
            remaining: toNum(r.remaining),
          }))
        );
      }
    } catch (e) {
      console.error(e);
      if (!cancelled) setError("Failed to load budget summaries for this month.");
    }

    try {
      // Income by source for the month
      const ir = await api.get(`/transactions/income-sources/${monthValue}`);
      const incomeList = Array.isArray(ir?.data) ? ir.data : [];
      if (!cancelled) {
        setIncomeRows(
          incomeList.map((r) => ({
            categoryId: r.categoryId,
            categoryName: r.categoryName ?? "(Unknown)",
            amount: toNum(r.amount),
          }))
        );
      }
    } catch (ie) {
      console.error(ie);
      if (!cancelled) setIncomeError("Failed to load income sources.");
    } finally {
      if (!cancelled) setLoading(false);
    }
  })();

  return () => {
    cancelled = true;
  };
}, [monthValue]);


  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return items.filter((it) => {
      if (it.type && it.type !== "EXPENSE") return false;
      if (hideZero && it.budgeted === 0 && it.spent === 0) return false;
      if (!q) return true;
      return it.categoryName.toLowerCase().includes(q);
    });
  }, [items, query, hideZero]);

  const sorted = useMemo(() => {
    const arr = [...filtered];
    arr.sort((a, b) => {
      const dir = sort.dir === "asc" ? 1 : -1;
      switch (sort.key) {
        case "category":
          return a.categoryName.localeCompare(b.categoryName) * dir;
        case "budgeted":
          return (a.budgeted - b.budgeted) * dir;
        case "spent":
          return (a.spent - b.spent) * dir;
        case "remaining":
          return (a.remaining - b.remaining) * dir;
        case "usedPct":
          return (usedPct(a) - usedPct(b)) * dir;
        default:
          return 0;
      }
    });
    return arr;
  }, [filtered, sort]);

  const totals = useMemo(
    () =>
      sorted.reduce(
        (acc, x) => {
          acc.budgeted += x.budgeted;
          acc.spent += x.spent;
          acc.remaining += x.remaining;
          return acc;
        },
        { budgeted: 0, spent: 0, remaining: 0 }
      ),
    [sorted]
  );

  const usagePct = totals.budgeted > 0 ? Math.round((totals.spent / totals.budgeted) * 100) : 0;
  const incomeTotal = useMemo(
    () => incomeRows.reduce((acc, r) => acc + (r.amount || 0), 0),
    [incomeRows]
  );

  const onHeaderClick = (key) => {
    setSort((prev) =>
      prev.key === key ? { key, dir: prev.dir === "asc" ? "desc" : "asc" } : { key, dir: "asc" }
    );
  };

  return (
    <div className="mx-auto max-w-6xl p-4">
      {/* Top bar */}
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-2">
          <Link to="/budgets" className="inline-flex items-center gap-1 text-slate-600 hover:text-slate-900">
            <ArrowLeft className="h-4 w-4" />
            <span>Overview</span>
          </Link>
        </div>

        <div className="flex flex-col items-start gap-2 sm:flex-row sm:items-center">
          <h1 className="text-xl font-semibold">{monthDisplay || fmtMonth(monthValue)}</h1>

          {/* Month switcher */}
          <div className="flex items-center gap-2">
            <label htmlFor="month" className="text-sm text-slate-600">Month</label>
            <input
              id="month"
              type="month"
              value={monthValue}
              onChange={(e) => {
                const next = e.target.value;
                setMonthValue(next);
                // Keep URL in sync so you can share/bookmark
                navigate(`/budgets/${next}`, { replace: true });
              }}
              className="rounded-lg border px-2 py-1 text-sm"
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

      {/* KPIs */}
      <div className="grid gap-3 sm:grid-cols-3">
        <Kpi title="Budgeted" value={currency.format(totals.budgeted)} />
        <Kpi title="Spent" value={currency.format(totals.spent)} />
        <Kpi
          title="Remaining"
          value={currency.format(totals.remaining)}
          emphasis={totals.remaining < 0 ? "danger" : "ok"}
        />
      </div>

      {/* Usage bar */}
      <div className="mt-4">
        <div className="mb-1 flex items-center justify-between text-xs text-slate-600">
          <span>Budget used</span>
          <span>{Math.max(0, Math.min(100, usagePct))}%</span>
        </div>
        <div className="h-2 w-full rounded-full bg-slate-200">
          <div
            className={`h-2 rounded-full ${totals.remaining < 0 ? "bg-red-500" : "bg-emerald-500"}`}
            style={{ width: `${Math.max(0, Math.min(100, usagePct))}%` }}
          />
        </div>
      </div>

      {/* Controls */}
      <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-2 rounded-lg border px-2 py-1">
            <Search className="h-4 w-4 text-slate-500" />
            <input
              className="w-48 bg-transparent text-sm outline-none"
              placeholder="Search category…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </div>
          <label className="flex cursor-pointer items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={hideZero}
              onChange={(e) => setHideZero(e.target.checked)}
            />
            Hide zero rows
          </label>
        </div>
      </div>

      {/* Table */}
      <div className="mt-3 overflow-x-auto rounded-2xl border bg-white shadow-sm">
        <table className="min-w-[760px] w-full border-separate border-spacing-0">
          <thead>
            <tr className="text-left">
              {[
                { key: "category", label: "Category" },
                { key: "budgeted", label: "Budgeted" },
                { key: "spent", label: "Spent" },
                { key: "remaining", label: "Remaining" },
                { key: "usedPct", label: "Used %" },
              ].map((col) => (
                <Th
                  key={col.key}
                  active={sort.key === col.key}
                  dir={sort.dir}
                  onClick={() => onHeaderClick(col.key)}
                >
                  {col.label}
                </Th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading
              ? Array.from({ length: 8 }).map((_, i) => (
                  <tr key={i} className="border-b last:border-0">
                    {Array.from({ length: 5 }).map((__, j) => (
                      <td key={j} className="px-3 py-3">
                        <div className="h-4 w-24 animate-pulse rounded bg-slate-200" />
                      </td>
                    ))}
                  </tr>
                ))
              : sorted.map((r) => {
                  const hasBudget = r.id != null;
                  const pct = usedPct(r);
                  return (
                    <tr key={r.categoryId ?? r.id ?? r.categoryName} className="border-b last:border-0">
                      {/* Category + "No budget" badge */}
                      <td className="px-3 py-3 font-medium">
                        <div className="flex items-center gap-2">
                          {r.categoryName}
                          {!hasBudget && (
                            <span
                              className="rounded-full border border-amber-300 bg-amber-50 px-2 py-0.5 text-xs text-amber-700"
                              title="This category has spending but no budget set for this month"
                            >
                              No budget
                            </span>
                          )}
                        </div>
                      </td>

                      {/* Budgeted: gray if no budget */}
                      <td className={`px-3 py-3 tabular-nums ${!hasBudget ? "text-slate-400" : ""}`}>
                        {currency.format(r.budgeted)}
                      </td>

                      <td className="px-3 py-3 tabular-nums">{currency.format(r.spent)}</td>

                      {/* Remaining: red when negative */}
                      <td className={`px-3 py-3 tabular-nums ${r.remaining < 0 ? "text-red-600" : ""}`}>
                        {currency.format(r.remaining)}
                      </td>

                      {/* Used % bar */}
                      <td className="px-3 py-3">
                        <div className="flex items-center gap-2">
                          <div className="h-1.5 flex-1 rounded-full bg-slate-200">
                            <div
                              className={`h-1.5 rounded-full ${r.remaining < 0 ? "bg-red-500" : "bg-emerald-500"}`}
                              style={{ width: `${Math.max(0, Math.min(100, pct))}%` }}
                            />
                          </div>
                          <div className="w-12 text-right text-sm tabular-nums">
                            {r.budgeted > 0 ? Math.max(0, Math.min(100, Math.round(pct))) : 0}%
                          </div>
                        </div>
                      </td>
                    </tr>
                  );
                })}

            {/* Totals row */}
            {!loading && (
              <tr className="font-semibold">
                <td className="px-3 py-3">Total</td>
                <td className="px-3 py-3 tabular-nums">{currency.format(totals.budgeted)}</td>
                <td className="px-3 py-3 tabular-nums">{currency.format(totals.spent)}</td>
                <td className={`px-3 py-3 tabular-nums ${totals.remaining < 0 ? "text-red-600" : ""}`}>
                  {currency.format(totals.remaining)}
                </td>
                <td className="px-3 py-3 tabular-nums">
                  {Math.max(0, Math.min(100, usagePct))}%
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
            {/* Income by Source */}
      <div className="mt-8 rounded-2xl border bg-white shadow-sm">
        <div className="flex items-center justify-between border-b px-4 py-3">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-600">
            Income by Source
          </h2>
        </div>

        {incomeError && (
          <div className="mx-4 my-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
            {incomeError}
          </div>
        )}

        <div className="overflow-x-auto">
          <table className="min-w-[520px] w-full border-separate border-spacing-0">
            <thead>
              <tr className="text-left">
                <th className="border-b bg-slate-50 px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-600">
                  Source
                </th>
                <th className="border-b bg-slate-50 px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-600">
                  Received
                </th>
                <th className="border-b bg-slate-50 px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-600">
                  %
                </th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                Array.from({ length: 3 }).map((_, i) => (
                  <tr key={i} className="border-b last:border-0">
                    <td className="px-3 py-3">
                      <div className="h-4 w-36 animate-pulse rounded bg-slate-200" />
                    </td>
                    <td className="px-3 py-3">
                      <div className="ml-auto h-4 w-24 animate-pulse rounded bg-slate-200" />
                    </td>
                    <td className="px-3 py-3">
                      <div className="ml-auto h-4 w-10 animate-pulse rounded bg-slate-200" />
                    </td>
                  </tr>
                ))
              ) : incomeRows.length === 0 ? (
                <tr>
                  <td className="px-3 py-4 text-slate-500" colSpan={3}>
                    No income recorded for this month.
                  </td>
                </tr>
              ) : (
                incomeRows
                  .slice()
                  .sort((a, b) => b.amount - a.amount)
                  .map((r) => {
                    const pct = incomeTotal > 0 ? Math.round((r.amount / incomeTotal) * 100) : 0;
                    return (
                      <tr key={r.categoryId ?? r.categoryName} className="border-b last:border-0">
                        <td className="px-3 py-3 font-medium">{r.categoryName}</td>
                        <td className="px-3 py-3 tabular-nums">{currency.format(r.amount)}</td>
                        <td className="px-3 py-3 tabular-nums">{pct}%</td>
                      </tr>
                    );
                  })
              )}

              {/* Totals row */}
              {!loading && (
                <tr className="font-semibold">
                  <td className="px-3 py-3">Total</td>
                  <td className="px-3 py-3 tabular-nums">{currency.format(incomeTotal)}</td>
                  <td className="px-3 py-3 tabular-nums">100%</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
      <AddBudgetModal
        open={openAddBudget}
        onClose={() => setOpenAddBudget(false)}
        defaultMonth={monthValue}
        categories={categories}
        blockedCategoryIds={blockedCategoryIds}
        loadingCats={loadingCats}
        catsError={catsError}
        refreshCats={refreshCats}
        onCreated={(b) => {
          const ym = toYm(b?.month);
          if (ym !== monthValue) return;
          
          const val = round2(toNum(b?.value));
          const cat = b.category;
          setItems(prev => 
            applyNewBudget(prev, {
              budgetId: b?.id,
              categoryId: cat?.id ?? null,
              categoryName: cat?.name ?? "",
              addValue: val,
            })
          );
        }}
      />
    </div>
  );
}

/* ---------- Presentational bits ---------- */

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

function Th({ children, active, dir, onClick }) {
  return (
    <th
      onClick={onClick}
      className={`cursor-pointer select-none border-b bg-slate-50 px-3 py-2 text-xs font-semibold uppercase tracking-wide ${
        active ? "text-slate-900" : "text-slate-600"
      }`}
      title="Sort"
    >
      <div className="flex items-center gap-1">
        <span>{children}</span>
        <span className="text-slate-400">{active ? (dir === "asc" ? "▲" : "▼") : ""}</span>
      </div>
    </th>
  );
}

/* ---------- Helpers ---------- */

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

function fmtMonth(ym) {
  if (!ym || typeof ym !== "string" || ym.length < 7) return ym || "";
  const [y, m] = ym.split("-").map(Number);
  const d = new Date(y, (m || 1) - 1, 1);
  return d.toLocaleDateString("en-US", { month: "long", year: "numeric" }); // "March 2025"
}

function toNum(v) {
  if (typeof v === "number") return v;
  if (v == null) return 0;
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

function usedPct(row) {
  return row.budgeted > 0 ? (row.spent / row.budgeted) * 100 : 0;
}

function round2(n) {
  return Math.round((Number(n) + Number.EPSILON) * 100) / 100;
}

function applyNewBudget(list, { budgetId, categoryId, categoryName, addValue }) {
  if (categoryId == null) return list;

  const idx = list.findIndex(r => String(r.categoryId) === String(categoryId));
  if (idx === -1) {
    // brand new row
    const budgeted = round2(addValue);
    return [
      ...list,
      {
        id: budgetId ?? null,
        categoryId,
        categoryName: categoryName || "(Uncategorized)",
        type: "EXPENSE",
        budgeted,
        spent: 0,
        remaining: budgeted,
      },
    ];
  }

  const row = list[idx];

  // already had a budget → create-only: leave it unchanged
  if (row.id != null) return list;

  // promote "No budget" row → still create-only (not modifying an existing budget)
  const budgeted = round2(addValue);
  const remaining = round2(budgeted - (row.spent ?? 0));
  const next = list.slice();
  next[idx] = { ...row, id: budgetId ?? null, budgeted, remaining };
  return next;
}
