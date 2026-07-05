# 📦 Easy Buy Inventory Management Architecture

This document provides a detailed explanation of the linkage and dynamic operations between the **Products Service** and the **Inventory Service** within the Easy Buy e-commerce platform.

---

## 1. Architectural Linkage Model

In a distributed microservice environment, the catalog database and the inventory database are decoupled. They synchronize and link through a single unique identifier: the **`productId` (UUID)**.

```mermaid
classDiagram
    class ProductService {
        +UUID productId
        +String title
        +Double price
        +String description
        +List~Category~ categories
    }
    class InventoryService {
        +Long id
        +UUID productId
        +String sku
        +String productName
        +Integer availableQuantity
        +Integer reservedQuantity
        +Integer reorderLevel
    }
    ProductService "1" -- "1" InventoryService : Linked via productId (UUID)
```

### 💡 Why store `productName` in both services?
The inventory service stores the `productName` locally (**database denormalization**) to achieve:
1. **Low Latency**: Queries like checking low-stock alerts (`GET /api/inventories/low-stock`) do not need to make synchronous REST calls to `products-service` to show the names of the flagged items.
2. **Service Autonomy**: Warehouse operators can manage stock, check locations, and handle items even if the product catalog service is offline.
3. **Audit Trails**: It keeps a snapshot of the item's name as it was labeled when cataloged in inventory.

---

## 2. Inventory State Variables
The inventory service maintains three critical quantity fields for every product SKU:

$$\text{Total Physical Stock} = \text{availableQuantity} + \text{reservedQuantity}$$

* **`availableQuantity`**: Units physically present on shelves that are **available for new purchases**.
* **`reservedQuantity`**: Units claimed by customers currently checking out. They are **withheld from the public catalog** to prevent double-selling but are not yet shipped.
* **`reorderLevel`**: Threshold quantity. If `availableQuantity` falls below this value, the system triggers alerts for replenishment.

---

## 3. Dynamic Workflow Lifecycle

### Flow A: Registering a New Product
When a catalog admin creates a product, the inventory record must be initialized:

```mermaid
sequenceDiagram
    actor Admin
    participant PS as Products Service
    participant IS as Inventory Service

    Admin->>PS: POST /api/products
    Note over PS: Generates UUID (e.g. b1fa4852...)
    PS-->>Admin: Returns Product Details with UUID
    Admin->>IS: POST /api/inventories
    Note over IS: Map UUID to SMART-WATCH-BLK SKU
    IS-->>Admin: Inventory Initiated (available=150)
```

---

### Flow B: Checkout & Stock Reservation
When a customer initiates checkout, stock is temporarily reserved to ensure a smooth transition to payment without stock stealing.

```mermaid
sequenceDiagram
    actor Customer
    participant COS as Cart & Order Service
    participant IS as Inventory Service
    participant Gateway as Payment Gateway

    Customer->>COS: POST /api/orders/{userId}/checkout
    Note over COS: Check Active Cart
    COS->>IS: POST /api/inventories/product/{productId}/reserve (qty = 2)
    Note over IS: Checks: availableQuantity >= 2
    Note over IS: Deducts: availableQuantity (150 -> 148)<br/>Increments: reservedQuantity (0 -> 2)
    IS-->>COS: 200 OK (Reservation Successful)
    Note over COS: Save Order as PENDING
    COS->>Customer: Redirect to Payment Gateway
```

---

### Flow C: Complete Transaction (Payment Success)
If the customer successfully pays, the reserved stock is officially committed and subtracted from physical inventory.

```mermaid
sequenceDiagram
    participant COS as Cart & Order Service
    participant IS as Inventory Service
    participant DB as Inventory Database

    COS->>COS: Payment Status = SUCCESS
    Note over COS: Set Order Status = CONFIRMED
    COS->>IS: POST /api/inventories/product/{productId}/release (qty = 2)
    Note over IS: Release reservation (reservedQuantity: 2 -> 0)
    Note over IS: Note: availableQuantity remains 148
    IS->>DB: Save permanent stock state
    IS-->>COS: 200 OK
```

---

### Flow D: Transaction Rollback (Payment Fails or Order Cancelled)
If payment fails, or the customer decides to abort the purchase, the reserved stock is returned to the public pool.

```mermaid
sequenceDiagram
    participant COS as Cart & Order Service
    participant IS as Inventory Service
    participant DB as Inventory Database

    COS->>COS: Payment Status = FAILED / ABORTED
    Note over COS: Set Order Status = CANCELLED
    COS->>IS: POST /api/inventories/product/{productId}/release (qty = 2)
    Note over IS: Increment: availableQuantity (148 -> 150)<br/>Decrement: reservedQuantity (2 -> 0)
    IS->>DB: Save restored stock state
    IS-->>COS: 200 OK
```

---

## 4. Concurrent Order Protection (Race Conditions)

Because the database reservation check uses transactional isolation (`@Transactional`), the database prevents **double selling** (over-allocation of stock):

```sql
-- Conceptual Query executed under isolation during reservation
SELECT available_quantity, reserved_quantity 
FROM inventory_items 
WHERE product_id = :productId FOR UPDATE;
```

If two transactions try to reserve the last `1` remaining item simultaneously:
1. Transaction A locks the row and finds `available_quantity = 1`. It updates `available_quantity = 0` and `reserved_quantity = 1`.
2. Transaction B blocks until Transaction A releases the row lock.
3. Once A releases the lock, Transaction B reads the updated row: `available_quantity = 0`.
4. Transaction B fails with `BusinessRuleException: Insufficient stock` and rolls back, ensuring the system never over-commits stock.
