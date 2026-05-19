# StockSim India - Indian Stock Market Simulator

A comprehensive, real-time virtual trading platform designed to simulate the Indian Stock Market. Built with modern web technologies, StockSim allows users to trade stocks, manage portfolios, and track market movements without any financial risk.

## 🚀 Features

- **Real-Time Market Simulation**: Live price updates and dynamic OHLC (Open, High, Low, Close) data generation using WebSockets.
- **User Authentication**: Secure login and registration using Spring Security and JWT.
- **Portfolio Management**: Buy and sell stocks, view current holdings, and track overall portfolio performance.
- **Transaction History**: Detailed logs of all trades (buy/sell) executed by the user.
- **Gamification**: Earn badges and achievements based on trading performance and milestones.
- **Interactive Dashboard**: Visually appealing dashboard with real-time charts and market insights.
- **Responsive Design**: Built with a mobile-first approach ensuring a seamless experience across all devices.

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.3
- **Data Access & Storage**: Spring Data JPA, Hibernate, MySQL Database
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **Security**: Spring Security, JSON Web Tokens (JWT)
- **Real-time Communication**: Spring WebSockets
- **Build Tool**: Maven
- **Other Tools**: Lombok for boilerplate code reduction

## 📂 Project Structure

- `src/main/java/com/stocksim/controller`: Handles incoming HTTP requests and routes.
- `src/main/java/com/stocksim/model`: Contains JPA entity classes (e.g., `User`, `Transaction`, `OHLCData`, `UserBadge`).
- `src/main/java/com/stocksim/repository`: Data access interfaces extending JpaRepository.
- `src/main/java/com/stocksim/security`: Configuration and filters for Spring Security and JWT.
- `src/main/java/com/stocksim/service`: Core business logic and market simulation services.
- `src/main/java/com/stocksim/scheduler`: Scheduled tasks (e.g., `OHLCScheduler`) to simulate continuous market data.
- `src/main/resources/templates`: Thymeleaf HTML templates for the frontend (auth, dashboard, transactions).
- `src/main/resources/application.properties`: Configuration settings (DB connection, server port, etc.).

## ⚙️ Local Setup Instructions

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Karthikeyancse-coder/Stock-market-simulator.git
   cd Stock-market-simulator
   ```

2. **Configure the Database**:
   - Install MySQL and create a database named `stocksim`.
   - Update the database credentials in `src/main/resources/application.properties`:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/stocksim
     spring.datasource.username=root
     spring.datasource.password=yourpassword
     ```

3. **Build the Project**:
   ```bash
   mvn clean install
   ```

4. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```
   The application will be accessible at `http://localhost:8080`.

## 📜 License
This project is licensed under the MIT License.
