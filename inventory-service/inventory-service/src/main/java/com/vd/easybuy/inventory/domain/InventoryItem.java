package com.vd.easybuy.inventory.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_sku", columnNames = "sku"),
        @UniqueConstraint(name = "uk_inventory_product_id", columnNames = "productId")
})
@Getter
@Setter
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID productId;

    @Column(nullable = false, length = 128)
    private String sku;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(nullable = false, length = 120)
    private String warehouseLocation;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Integer reservedQuantity;

    @Column(nullable = false)
    private Integer reorderLevel;

    @Column(nullable = false)
    private boolean active;

    private Integer totalQuantity;

    private Boolean lowStock;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private String reasonToAdjustQuantity;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (availableQuantity == null) {
            availableQuantity = 0;
        }
        if (reservedQuantity == null) {
            reservedQuantity = 0;
        }
        if (reorderLevel == null) {
            reorderLevel = 0;
        }
    }


    @PreUpdate
    void onUpdate(){
        updatedAt=Instant.now();
    }

}
