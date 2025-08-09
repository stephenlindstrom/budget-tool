import { useState, useEffect } from "react";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";
import api from "../api/api";

import BudgetForm from "../components/budgets/BudgetForm";
import CategoryModal from "../components/categories/CategoryModal";

export default function CreateBudgetPage() {
  const { loading } = useAuth();
  const navigate = useNavigate();

  const defaultMonth = new Date().toISOString().slice(0, 7);
  
  const [categories, setCategories] = useState([]);
  const [catsError, setCatsError] = useState("");
  const [loadingCats, setLoadingCats] = useState(false);
  
  const [isCatModalOpen, setCatModalOpen] = useState(false);

  // Auth + categories fetch
  useEffect(() => {
    if (loading) return;

    const controller = new AbortController();
    (async () => {
      setLoadingCats(true);
      setCatsError("");
      try {
        const res = await api.get("/categories", { signal: controller.signal });
        setCategories(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        if (controller.signal.aborted) return;
        console.error(err);
        setCatsError(err.response?.data?.message || "Error fetching categories");
      } finally {
        if (!controller.signal.aborted) setLoadingCats(false);
      }
    })();

    return () => controller.abort();

  }, [loading, navigate]);

  // Called by BudgetForm when user submits a new budget
  const submitBudget = async (payload) => {
    // payload = { month, value:number, categoryId:number }
    return api.post("/budgets", payload);
  };

  // Called by CategoryModal to create a new category
  const createCategory = async (name, type) => {
    const res = await api.post("/categories", { name, type });
    const created = res.data; // { id, name, type }
    // Update Local List so it appears in the dropdown immediately
    setCategories((prev) => [created, ...prev]);
    return created;
  };

  return (
    <div style={{ maxWidth: 640, margin: "2rem auto" }}>
      <h2>Create Budget</h2>

      {/* Page-level fetch error (form-level errors are handled inside BudgetForm) */}
      {catsError && (
        <p role="alert" style={{ color: "red" }}>
          <strong>Error:</strong> {catsError}
        </p>
      )}

      <BudgetForm
        categories={categories}
        loadingCats={loadingCats}
        defaultMonth={defaultMonth}
        onSubmit={submitBudget}
        onAddCategoryClick={() => setCatModalOpen(true)}
      />

      <CategoryModal
        isOpen={isCatModalOpen}
        onClose={() => setCatModalOpen(false)}
        onCreate={createCategory}
        categories={categories}
      />
    </div>
  );
}