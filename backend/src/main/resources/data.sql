-- Insert categories
INSERT INTO category (id, name, type) VALUES
  (1, 'Groceries', 'EXPENSE'),
  (2, 'Rent', 'EXPENSE'),
  (3, 'Salary', 'INCOME');

-- Insert budgets
INSERT INTO budget (id, budget_value, budget_month, category_id) VALUES
  (1, 500.00, '2025-06', 1),
  (2, 1500.00, '2025-06', 2);

-- Insert transactions
INSERT INTO transaction (id, amount, transaction_date, description, type, category_id) VALUES
  (1, 75.00, '2025-06-03', 'Walmart', 'EXPENSE', 1),
  (2, 100.00, '2025-06-05', 'Target', 'EXPENSE', 1),
  (3, 3000.00, '2025-06-01', 'Paycheck', 'INCOME', 3);