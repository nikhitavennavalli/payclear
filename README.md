# PayClear - Intelligent Payment Management and Automated Reconciliation

PayClear is a financial technology transaction engine and automated reconciliation application. It streamlines merchant payment tracking, calculates real-time transaction processing fees and GST deductions, renders live financial analytics, and provides an AI-driven Natural Language Text-to-SQL assistant for intuitive query parsing.

## Key Features

* Automated Fee and Reconciliation Engine: Automatically computes a 2% transaction processing fee and 18% GST deduction on gross sales to determine net merchant payouts.
* Text-to-SQL AI Assistant: Enables non-technical users to query database transactions using natural language such as show successful transactions above 999 or show UPI payments.
* Interactive Financial Analytics: Integrates Chart.js to render real-time payment method breakdowns across UPI, Card, and Net Banking, as well as gross-versus-net revenue summaries.
* Enterprise Guardrails and Validation: Implements input validation for payment inputs and a central global exception handler to prevent internal stack trace exposure.
* Dynamic Search and Filtering: Provides real-time merchant search filtering and transaction status updates including SUCCESS, PENDING, and REFUNDED.

## Tech Stack

* Backend: Java 17, Spring Boot 3, Spring Data JPA
* Database: H2 In-Memory Database for development, PostgreSQL ready for production
* Frontend: HTML5, CSS3, Vanilla JavaScript, Chart.js
* Build Tool: Maven

## Prerequisites

* Java Development Kit JDK 17 or higher
* Git

## Getting Started and Local Setup

1. Clone the Repository:
git clone [https://github.com/nikhitavennavalli/payclear.git](https://www.google.com/search?q=https://github.com/nikhitavennavalli/payclear.git)
cd payclear
2. Build and Run the Application:
./mvnw clean package -DskipTests
./mvnw spring-boot:run
3. Access the Dashboard:
Open your web browser and navigate to http://localhost:8080/index.html

## API Endpoints

* POST /api/payments : Record a new payment with validation for payer name and positive amounts
* GET /api/payments : Fetch all payments or search by payer name using the search parameter
* GET /api/payments/reconcile : Get calculated gross volume, fee deductions, and net expected payouts
* POST /api/payments/ai-query : Process natural language queries into generated SQL and filtered datasets
* PUT /api/payments/{id}/status : Update transaction or payout status
* DELETE /api/payments/{id} : Remove a transaction record

