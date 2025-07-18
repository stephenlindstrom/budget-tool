# 💰 Personal Finance Tracker

A backend REST API for tracking personal income, expenses, and budgets. Built with **Java (Spring Boot)** and tested with **JUnit/MockMvc**, this project demonstrates clean architecture, database integration, Docker-based deployment, and a focus on real-world use cases.

A React frontend is planned for future development. The backend is deployed on **Render**, running an H2 in-memory demo database preloaded with data for immediate testing via Swagger UI.

---

## ✅ Key Features

- Add, update, and delete transactions
- Assign categories and transaction types (income/expense)
- Set monthly budgets by category
- Filter transactions by date, category, and type
- Retrieve saved budget months
- Swagger UI for API exploration and demo
- Full test coverage (unit + integration)
- Profiles for local PostgreSQL and deployed H2

---

## 🔧 Tech Stack

- **Java 21**, **Spring Boot**, **Spring Data JPA**
- **PostgreSQL** (local), **H2 In-Memory DB** (deployed)
- **JUnit 5**, **Mockito**, **MockMvc**
- **Docker**, **Swagger/OpenAPI**
- CI/CD via **GitHub Actions** *(planned)*
- Frontend: **React + Axios** *(planned)*

---

## 🚀 Live Demo

The app is deployed on **Render** using Docker and runs with an **H2 in-memory database** preloaded with demo data.  
Explore it via the Swagger UI:

🔗 [https://budget-backend-gkce.onrender.com/swagger-ui/index.html](https://budget-backend-gkce.onrender.com/swagger-ui/index.html)

---

## 🧪 Test Coverage

- ✅ **Service Layer Unit Tests** with JUnit + Mockito
- ✅ **Controller Integration Tests** using MockMvc and test profile
- Validates business logic and endpoint reliability

---

## 🖥️ Running Locally

### Requirements

- Java 21
- Maven
- PostgreSQL (for local development with production profile)

### 1. Clone and navigate to project

```bash
git clone https://github.com/stephenlindstrom/budget-tool.git
```

### 2. Set Up PostgreSQL

Make sure PostgreSQL is installed and running locally.

Create a database and user that match the credentials defined in application-prod.properties:

```sql
CREATE DATABASE budget_db;
CREATE USER budget_user WITH ENCRYPTED PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE budget_db TO budget_user;
```

### 3. Run with production profile

This command starts the application using the `prod` profile, which connects to your local PostgreSQL database:

```bash
./mvnw -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=prod
```

### 4. Access Swagger UI

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🔁 Spring Profiles

This project uses [Spring Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles) to support multiple environments:

| Profile     | Description                                                      | Database       | Purpose                          |
|-------------|------------------------------------------------------------------|----------------|----------------------------------|
| `demo`      | Loads demo data into an in-memory database                       | H2 (in-memory) | Deployed to Render for showcasing the app |
| `prod`      | Connects to a local PostgreSQL database with persistent data     | PostgreSQL     | Used when running locally with real data  |
| `test`      | Used during automated tests with isolated test data              | H2 (in-memory) | Ensures clean testing environment |

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
│       │       ├── application.properties
│       │       ├── application-demo.properties
│       │       ├── application-prod.properties
│       │       └── data.sql
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

---

## 🚧 Planned Features

- **User Authentication**
  - JWT-based login, signup, and logout functionality

- **Frontend Dashboard (React)**
  - Responsive user interface
  - Monthly income vs. expense summary
  - Visualizations like pie charts and bar graphs by category

- **Recurring Transactions**
  - Support for subscriptions, rent, and other repeating expenses

- **CSV Import/Export**
  - Upload and download transactions for easier record-keeping

- **CI/CD Pipeline**
  - Automated testing and deployment via GitHub Actions

- **Cloud Deployment Enhancements**
  - Migrate from in-memory to persistent cloud database (e.g., PostgreSQL on Render)

---

## 👤 Author

**Stephen Lindstrom**  
- GitHub: [@stephenlindstrom](https://github.com/stephenlindstrom)  
- LinkedIn: [Stephen Lindstrom](https://www.linkedin.com/in/stephen-lindstrom)
