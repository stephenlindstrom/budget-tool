-- Insert categories
INSERT INTO category (id, name, type) VALUES
  (1, 'Groceries', 'EXPENSE'),
  (2, 'Rent', 'EXPENSE'),
  (3, 'Salary', 'INCOME'),
  (4, 'Dining', 'EXPENSE'),
  (5, 'Utilities', 'EXPENSE'),
  (6, 'Freelance', 'INCOME'),
  (7, 'Entertainment', 'EXPENSE'),
  (8, 'Investments', 'INCOME'),
  (9, 'Travel', 'EXPENSE'),
  (10, 'Gifts', 'EXPENSE');

-- Insert budgets
INSERT INTO budget (id, budget_value, budget_month, category_id) VALUES
  (1, 500.00, '2025-05', 1),
  (2, 1500.00, '2025-03', 2),
  (3, 3000.00, '2025-01', 3),
  (4, 200.00, '2024-12', 4),
  (5, 250.00, '2025-04', 5),
  (6, 1000.00, '2024-11', 6),
  (7, 150.00, '2025-06', 7),
  (8, 500.00, '2025-02', 8),
  (9, 800.00, '2024-10', 9),
  (10, 100.00, '2024-09', 10);

-- Insert transactions
INSERT INTO transaction (id, amount, transaction_date, description, type, category_id) VALUES
  (1, 75.00, '2025-05-15', 'Walmart', 'EXPENSE', 1),
  (2, 100.00, '2025-03-10', 'Target', 'EXPENSE', 1),
  (3, 3000.00, '2025-01-31', 'Paycheck', 'INCOME', 3),
  (4, 60.00, '2024-12-05', 'Chipotle', 'EXPENSE', 4),
  (5, 120.00, '2025-04-25', 'Electric Bill', 'EXPENSE', 5),
  (6, 750.00, '2024-11-19', 'Freelance Project', 'INCOME', 6),
  (7, 45.00, '2025-06-10', 'Movie Night', 'EXPENSE', 7),
  (8, 200.00, '2025-02-08', 'Dividends', 'INCOME', 8),
  (9, 500.00, '2024-10-20', 'Flight to Chicago', 'EXPENSE', 9),
  (10, 80.00, '2024-09-14', 'Birthday Gift', 'EXPENSE', 10);



