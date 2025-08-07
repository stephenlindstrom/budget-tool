import { Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import BudgetSummaryPage from "./pages/BudgetSummaryPage";
import CreateBudgetPage from "./pages/CreateBudgetPage";

function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/dashboard" element={<DashboardPage />} />
      <Route path="/budgets/:value" element={<BudgetSummaryPage />} />
      <Route path="/create_budget" element={<CreateBudgetPage />} />
    </Routes>
  );
}

export default App;