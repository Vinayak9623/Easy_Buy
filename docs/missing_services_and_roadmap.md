# 🗺️ Project Completeness Roadmap: Missing Services & Integrations

This document lists the remaining services in the Easy Buy workspace that are currently empty (skeleton projects) and details what is required to complete them for a full production-ready backend.

---

## 1. Project Status Matrix

Below is the implementation status of all services in the `easy_busy` folder:

| Service Name | Primary Responsibility | Current Status | Next Action |
| :--- | :--- | :--- | :--- |
| **`users-service`** | User Account & Profile CRUD | **🟢 Completed** | Ready for DB integration |
| **`products-service`** | Category, Product, & Review Catalog | **🟢 Completed** | Production ready |
| **`inventory-service`** | Warehouse Stock & Locks | **🟢 Completed** | Pessimistic locking verified |
| **`cart-order-service`** | Shopping Cart & Order Rollbacks | **🟢 Completed** | Inter-service sync verified |
| **`payment-service`** | Billing, Gateways & Transactions | **🔴 Empty Skeleton** | Needs API & DB Schema |
| **`notifications-service`** | E-mail, SMS, & Push alerts | **🔴 Empty Skeleton** | Needs Sender Logic & APIs |
| **`ai-service`** | Smart Search & Product Suggestions | **🔴 Empty Skeleton** | Optional integration |
| **`api-gateway`** | Routing, Port gateway (8080) | **🟢 Completed** | Active |
| **`service-discovery`** | Eureka Server registry | **🟢 Completed** | Active |
| **`config-server`** | Central properties server | **🟢 Completed** | Active |

---

## 2. Deep Dive: What Needs to be Implemented

### A. Payment Service (`payment-service`)
Currently only contains the main Spring Boot runner. It is responsible for processing actual card/wallet transactions.

* **Required Entities**:
  * `Transaction`: stores `transactionId`, `orderId`, `amount`, `paymentMethod`, `status` (PENDING, SUCCESS, FAILED), and `paymentGatewayTxnId`.
* **API Endpoints**:
  * `POST /api/payments` - Process payment payload.
  * `GET /api/payments/order/{orderId}` - Fetch payment details for an order.
* **Cart-Order Integration**:
  * The `cart-order-service` checkout flow should execute a Feign call to `/api/payments`. If the payment fails, the checkout transaction must roll back and release the reserved stock.

---

### B. Notifications Service (`notifications-service`)
Currently only contains the main Spring Boot runner. It is responsible for communicating with clients.

* **Required Utilities**:
  * Spring Mail integration (`JavaMailSender`).
* **API Endpoints**:
  * `POST /api/notifications/email` - Send an email with variable template parameters.
  * Send triggers: `Order Confirmation`, `User Welcome Sign Up`, `Out of Stock Alerts`, `Payment Failure Warnings`.
* **Asynchronous Integration**:
  * As discussed in the [future_architectural_improvements.md](file:///D:/Live%20Batches/micro_devops/easy_busy/docs/future_architectural_improvements.md), this service is the primary candidate to run as a Kafka consumer subscribing to `order-events` or `user-events`.

---

### C. AI Service (`ai-service`)
Currently only contains the main Spring Boot runner. 

* **Suggested Implementations**:
  * Spring AI or Ollama client integration.
  * **API Endpoints**:
    * `GET /api/ai/recommendations/{userId}` - Recommend products based on order history.
    * `POST /api/ai/generate-description` - Help sellers auto-generate rich long descriptions for products.
