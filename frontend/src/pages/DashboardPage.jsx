import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/api";

function DashboardPage() {
  const [months, setMonths] = useState([]);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const fetchMonths = async () => {
      try {
        const res = await api.get("/budgets/months");
        setMonths(res.data);
      } catch (err) {
        setError("Failed to load available months.");
        console.error(err);
      }
    };

    fetchMonths();
  }, []);

  return (
    <div style={{ maxWidth: 600, margin: "2rem auto"}}>
      <h2>Available Budget Months</h2>
      {error && <p style={{ color: "red"}}>{error}</p>}
      <ul>
        {months.map(({ value, display }) => (
          <li key={value}>
            <button onClick={() => navigate(`/budgets/${value}`)}>
              {display}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default DashboardPage;