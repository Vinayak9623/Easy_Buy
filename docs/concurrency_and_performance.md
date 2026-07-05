# ⚡ Concurrency Control & Performance Impact

This document explains how the Easy Buy platform handles multiple customers buying the same product at the same time (Concurrency) and how it affects system performance.

---

## 1. The Concurrency Problem (Race Condition)

Imagine there is only **1 item** left in stock.
* **Customer A** and **Customer B** click "Buy Now" at the exact same millisecond.
* Without concurrency control, both requests read the stock as `1`.
* Both requests subtract `1` and update the stock to `0`.
* **Result**: The product is sold twice (Double Selling). This causes business losses and customer anger.

---

## 2. Our Solution: Pessimistic Write Locking

We resolved this issue by implementing **Pessimistic Write Locking** (`PESSIMISTIC_WRITE`) in the database.

* When a customer starts the checkout process, the transaction locks that specific product row in the database.
* Other customers trying to buy the **same product** must wait in queue until the first customer's transaction is finished.
* When the lock is released, the next customer reads the updated stock (which is now `0`) and receives a clean *"Out of Stock"* error message.

---

## 3. Does This Affect Performance?

Yes, there is a minor trade-off, but it is highly controlled. Here is the detailed breakdown:

### A. No Impact on Different Products
* Database locking is done at the **Row Level**, not the Table Level.
* If Customer A buys an iPhone and Customer B buys a Smart Watch, they lock different rows. **They do not block each other.**
* The system processes these orders concurrently with zero performance impact.

### B. Impact During Flash Sales (Same Product)
* If 1,000 customers try to buy the **exact same product** at once, their requests are queued sequentially.
* This adds a minor latency (delay of a few milliseconds) for customers at the end of the queue.
* However, this delay is necessary to guarantee database consistency and prevent double-selling.

### C. Normal Browsing is Unaffected
* Lock queries are only triggered during active writes (Checkout, Adjust Stock, Reserve, Release).
* Customers browsing the catalog, searching for items, or viewing details **never acquire locks**.
* Read-only operations remain extremely fast.

---

## 4. Alternative: Optimistic Locking (`@Version`)

If zero-blocking is required, we can switch to **Optimistic Locking**:
* A `@Version` column is added to the table.
* The system does not lock rows. Instead, it rejects updates if the version has changed.
* **Pros**: No database locks or waiting queues.
* **Cons**: Many transactions will fail, requiring complex application retry logic which increases server CPU load.

---

## 💡 Summary Design Decision
For e-commerce inventory, **Pessimistic Locking** is the standard industry choice. Keeping the database write transactions short (only performing stock check + update) minimizes the lock duration, keeping the latency negligible for the end users under normal and high workloads.
