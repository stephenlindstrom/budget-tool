import { Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import BudgetSummaryPage from "./pages/BudgetSummaryPage";
import CreateBudgetPage from "./pages/CreateBudgetPage";
import { useAuth } from "./hooks/useAuth";
import { useEffect } from "react";
import { configureApi } from "./api/api";
import ProtectedLayout from "./routes/ProtectedLayout";

function App() {
  const { token, logout } = useAuth();

  useEffect(() => {
    configureApi({
      getToken: () => token,
      onUnauthorized: () => logout(),
    });
  }, [token, logout]);

  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route element={<ProtectedLayout />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/budgets/:value" element={<BudgetSummaryPage />} />
        <Route path="/create_budget" element={<CreateBudgetPage />} />
      </Route>
    </Routes>
  );
}

export default App;