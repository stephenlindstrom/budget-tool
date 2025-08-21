import { Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import BudgetSummaryPage from "./pages/BudgetSummaryPage";
import CreateBudgetPage from "./pages/CreateBudgetPage";
import { useAuth } from "./hooks/useAuth";
import { useEffect } from "react";
import { configureApi } from "./api/api";
import ProtectedLayout from "./routes/ProtectedLayout";
import TransactionPage from "./pages/TransactionPage";
import RegistrationPage from "./pages/RegistrationPage";
import BudgetOverviewPage from "./pages/BudgetOverviewPage";
import BudgetMonthPage from "./pages/BudgetMonthPage";

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
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegistrationPage />} />
      <Route element={<ProtectedLayout />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/create_budget" element={<CreateBudgetPage />} />
        <Route path="/transactions" element={<TransactionPage />} />
        <Route path="/budgets" element={<BudgetOverviewPage />} />
        <Route path="/budgets/:month" element={<BudgetMonthPage />} />
      </Route>
    </Routes>
  );
}

export default App;