package com.vd.easybuy.inventory.repository;

import com.vd.easybuy.inventory.domain.InventoryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryItem,Long> {

    Optional<InventoryItem> findBySku(String sku);

    Optional<InventoryItem> findByProductId(UUID productId);

    //find inventory by inventory item id
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryItem i where i.id= :id")
    Optional<InventoryItem> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryItem i where i.productId= :productId")
    Optional<InventoryItem> findByProductIdForUpdate(@Param("productId") UUID productId);

    boolean existsBySku(String sku);

    boolean existsByProductId(UUID productId);

    List<InventoryItem> findByActiveTrueOrderByProductNameAsc();

    List<InventoryItem> findByAvailableQuantityLessThanEqualAndActiveTrueOrderByAvailableQuantityAsc(int threshold);

}
