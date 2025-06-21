# 💰 Personal Finance Tracker

A full-stack personal finance web application that enables users to track income, expenses, and budgets through a simple, intuitive interface. The backend is built with **Java (Spring Boot)**, while a **React** frontend is planned but not yet implemented.

---

## ✅ Current Features

* Add and delete income and expense transactions
* Categorize transactions (e.g., Groceries, Utilities, Salary)
* Set monthly budgets per category
* View and filter transactions by type, category, and date range
* Get a list of all available months with budgets
* Data persisted in a PostgreSQL database
* Full unit tests for services and integration tests for all controllers

---

## 🔧 Tech Stack

### Backend

* Java 17
* Spring Boot
* Spring Data JPA
* PostgreSQL
* JUnit 5 (unit and integration testing)
* MockMvc + Mockito

### Frontend *(In Progress / Planned)*

* React
* Axios
* Material UI

### Dev Tools

* Git + GitHub
* VS Code
* PostgreSQL (local dev)
* Docker (planned)
* CI/CD via GitHub Actions (planned)

---

## 📌 Planned Features

* JWT-based user authentication (signup/login/logout)
* Responsive frontend dashboard

  * Monthly income vs. expense summary
  * Category breakdown charts (pie, bar, etc.)
* Recurring transactions (subscriptions, rent, etc.)
* CSV import/export
* Cloud deployment via Render

---

## 🗪 Testing Coverage

* ✅ **Unit Tests**: All service layers tested with JUnit + Mockito
* ✅ **Integration Tests**: All controller endpoints tested using MockMvc with H2 test database
* ✅ Focus on reliability, maintainability, and real-world behavior validation

---

## 📂 Folder Structure

```bash
budget-tool/
├── .gitignore
├── README.md
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/
│       │   │       └── stephenlindstrom/
│       │   │           └── financeapp/
│       │   │               └── budget_tool/
│       │   │                   ├── config/
│       │   │                   ├── controller/
│       │   │                   ├── converter/
│       │   │                   ├── dto/
│       │   │                   ├── enums/
│       │   │                   ├── errors/
│       │   │                   ├── model/
│       │   │                   ├── repository/
│       │   │                   └── service/
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           ├── java/
│           │   └── com/
│           │       └── stephenlindstrom/
│           │           └── financeapp/
│           │               └── budget_tool/
│           │                   ├── integration/
│           │                   └── service/
│           └── resources/
│               └── application-test.properties

```
