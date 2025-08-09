import { Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import BudgetSummaryPage from "./pages/BudgetSummaryPage";
import CreateBudgetPage from "./pages/CreateBudgetPage";
import { useAuth } from "./hooks/useAuth";
import { useEffect } from "react";
import { configureApi } from "./api/api";
import ProtectedRoute from "./routes/ProtectedRoute";

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
      <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
      <Route path="/budgets/:value" element={<ProtectedRoute><BudgetSummaryPage /></ProtectedRoute>} />
      <Route path="/create_budget" element={<ProtectedRoute><CreateBudgetPage /></ProtectedRoute>} />
    </Routes>
  );
}

export default App;