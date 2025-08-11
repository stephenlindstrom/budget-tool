import React, { Fragment, useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth";
import api from "../api/api";

function TransactionPage() {
  const { loading } = useAuth();

  const [transactions, setTransactions] = useState([]);
  const [transError, setTransError] = useState("");
  const [loadingTrans, setLoadingTrans] = useState(false);

  const dateFormatter = new Intl.DateTimeFormat("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });

  const currencyFormatter = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2,
  })

  useEffect(() => {
    if (loading) return;

    const controller = new AbortController();
    (async () => {
      setLoadingTrans(true);
      setTransError("");
      try {
        const res = await api.get("/transactions", { signal: controller.signal });
        setTransactions(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        if (controller.signal.aborted) return;
        console.error(err);
        setTransError(err.response?.data?.message || "Error fetching transactions");
      } finally {
        if (!controller.signal.aborted) setLoadingTrans(false);
      }
    })();

    return () => controller.abort();

  }, [loading]);
  
  return (
    <div className="container mt-16 mb-24">
      <h2>Transactions</h2>

      {transError && (
        <p className="alert alert--error" role="alert">
          <strong>Error:</strong> {transError}
        </p>
      )}

      {loadingTrans ? (
        <p aria-live="polite">Loading transactions...</p>
      ) : transactions.length === 0 ? (
        <p>No transactions found.</p>
      ) : (
        <>
          <table className="table">
            <caption className="sr-only">List of transactions</caption>
            <thead>
              <tr>
                <th scope="col">Date</th>
                <th scope='col'>Description</th>
                <th scope="col">Amount</th>
              </tr>
            </thead>

            <tbody>
            {transactions.map(({ id, amount, date, description }) => (
                  <tr key={id}>
                    <td>{dateFormatter.format(new Date(date))}</td>
                    <td>{description}</td>
                    <td>{currencyFormatter.format(amount ?? 0)}</td>
                  </tr>  
              ))}
              </tbody>
          </table>
        </>
      )}
    </div>
  )
}

export default TransactionPage;