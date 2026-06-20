package com.vd.easybuy.inventory.service;

import com.vd.easybuy.inventory.dto.*;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    InventoryResponse create(CreateInventoryRequest request);

    InventoryResponse update(Long id, UpdateInventoryRequest request);

    InventoryResponse getById(Long id);

    InventoryResponse getBySku(String sku);

    InventoryResponse getByProductId(UUID productId);

    List<InventoryResponse> getAll();

    List<InventoryResponse> getLowStock(int threshold);

    InventoryResponse adjustStock(Long id, AdjustStockRequest request);

    InventoryResponse reserveStock(Long id, ReserveStockRequest request);

    InventoryResponse releaseStock(Long id, ReleaseStockRequest request);

    InventoryResponse reserveStockByProductId(UUID productId,ReserveStockRequest request);

    InventoryResponse releaseStockByProductId(UUID productId,ReleaseStockRequest request);

    void delete(Long id);
}
