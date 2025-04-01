# 💰 Personal Finance Tracker

A full-stack personal finance web application that helps users track income, expenses, and budgets using a clean and intuitive interface. Built with **Java (Spring Boot)** on the backend and **React** on the frontend.

---

## 📌 Features (MVP)

- User authentication with JWT (signup, login, logout)
- Add, edit, and delete income and expense transactions
- Categorize transactions (e.g. Rent, Groceries, Salary)
- Dashboard with:
  - Monthly income vs. expenses
  - Expense breakdown by category (pie chart)
  - Expense trends over time (bar/line chart)
- Filter transactions by date or category
- Secure, mobile-responsive UI

---

## 🔧 Tech Stack

### Frontend
- React
- Axios
- Tailwind CSS or Material UI (TBD)
- Recharts or Chart.js

### Backend
- Java 17
- Spring Boot
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL

### DevOps / Tools
- Git + GitHub
- Docker (planned)
- GitHub Actions (planned)
- Vercel (frontend deployment)
- Render or Railway (backend deployment)

---

## 🚧 Planned Features

- Set budgets by category and track progress
- Recurring transactions (e.g., subscriptions, rent)
- CSV export/import
- Receipt upload (image or PDF)
- Email notifications for upcoming bills or overspending
- AI budgeting suggestions (GPT API integration)

---

## ☁️ Future Architecture (Microservices Phase)

This app will evolve into a microservices architecture to demonstrate scalable system design:

- `User Service` – auth, user profile
- `Transaction Service` – expense/income management
- `Analytics Service` – trends, budgeting, insights
- `Notification Service` – reminders via email/SMS

Each service will run independently, containerized with **Docker**, and orchestrated with **Docker Compose** or deployed to **cloud platforms** like AWS, GCP, or Render.

---

## 📂 Folder Structure (Initial Monolith)

```bash
project-root/
│
├── backend/         # Spring Boot application
│   ├── src/main/java/com/yourapp/...
│   └── resources/application.properties
│
├── frontend/        # React application
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/
│       └── context/
