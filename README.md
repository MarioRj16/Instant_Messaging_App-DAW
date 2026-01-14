# Instant Messaging App

This project is an instant messaging application similar to WhatsApp, designed to facilitate real-time communication between users.

## Architecture

The application is structured into two main components:

- **Backend**: Built using **Spring Boot** (running on the JVM). It handles business logic, data persistence, and API endpoints.
- **Frontend**: Built using **React**. It provides the user interface for interacting with the application.

## Documentation

For a detailed analysis of the software architecture and design, please refer to the [System Architecture Document](docs/SAD.pdf) located in the `docs` folder.

## How to Run

### Prerequisities
- Node.js and npm
- Java Development Kit (JDK 17 or higher recommended)
- Docker (for database and containerized execution)

### Backend (Spring Boot)
1. Navigate to the JVM project directory:
   ```bash
   cd code/jvm
   ```
2. Run the application:
   ```bash
   ./gradlew bootRun
   ```
   Alternatively, you can use Docker Compose to set up the environment:
   ```bash
   docker-compose up
   ```

### Frontend (React)
1. Navigate to the JS project directory:
   ```bash
   cd code/js
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm start
   ```

