package com.vd.easybuy.inventory.service;

import com.vd.easybuy.common.payload.ProductSnapshot;
import com.vd.easybuy.inventory.domain.InventoryItem;
import com.vd.easybuy.inventory.dto.*;
import com.vd.easybuy.inventory.exception.BusinessRuleException;
import com.vd.easybuy.inventory.exception.ResourceNotFoundException;
import com.vd.easybuy.inventory.external.ProductClient;
import com.vd.easybuy.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;
    private final ProductClient productClient;

    public InventoryServiceImpl(InventoryRepository repository,
                                ProductClient productClient) {
        this.repository = repository;
        this.productClient = productClient;

    }


    @Override
    public InventoryResponse create(CreateInventoryRequest request) {

        ProductSnapshot productSnapshot = null;
        try {
            productSnapshot = this.productClient.getProductById(request.productId());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Product not found");
        }

        String sku = normalizeSku(request.sku());

        if (repository.existsBySku(sku)) {
            throw new BusinessRuleException("Inventory alredy exist for productId:" + request.productId());
        }
        InventoryItem item = new InventoryItem();
        item.setProductId(request.productId());
        item.setSku(sku);
        item.setProductName(trim(productSnapshot.title()));
        item.setWarehouseLocation(trim(request.warehouseLocation()));
        item.setAvailableQuantity(defaultZero(request.availableQuantity()));
        item.setReservedQuantity(defaultZero(request.reservedQuantity()));
        item.setReorderLevel(defaultZero(request.reorderLevel()));
        item.setActive(request.active() == null || request.active());

        return toResponse(repository.save(item));
    }

    @Override
    public InventoryResponse update(Long id, UpdateInventoryRequest request) {
        InventoryItem item = findEntity(id);

        item.setProductName(request.productName());
        item.setWarehouseLocation(request.warehouseLocation());
        item.setReorderLevel(request.reorderLevel());
        item.setActive(request.active());
        return toResponse(repository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getBySku(String sku) {
        return toResponse(repository.findBySku(normalizeSku(sku))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory not for sku: " + sku)));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(UUID productId) {
        return toResponse(repository.findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("inventory not foun with product: " + productId)));
    }

    @Override
    public List<InventoryResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStock(int threshold) {
        return repository.findByAvailableQuantityLessThanEqualAndActiveTrueOrderByAvailableQuantityAsc(threshold)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InventoryResponse adjustStock(Long id, AdjustStockRequest request) {
        InventoryItem item = findEntityForUpdate(id);

        int delta = request.quantityDelta();
        int nextAvailable = safeInt(item.getAvailableQuantity()) + delta;

        if (nextAvailable < 0) {
            throw new BusinessRuleException("Adjustment would make available quantity negative");
        }
        item.setAvailableQuantity(nextAvailable);
        item.setReasonToAdjustQuantity(trim(request.reason()));
        return toResponse(repository.save(item));
    }

    @Override
    public InventoryResponse reserveStock(Long id, ReserveStockRequest request) {
        InventoryItem item =findEntityForUpdate(id);
        int quantity=request.quantity();
        int available=safeInt(item.getAvailableQuantity());
        if(available<quantity){
            throw new BusinessRuleException("Insufficiant available stock to reserve");
        }
        item.setAvailableQuantity(available-quantity);
        item.setReservedQuantity(safeInt(item.getReservedQuantity())+quantity);
        return toResponse(repository.save(item));
    }

    @Override
    public InventoryResponse releaseStock(Long id, ReleaseStockRequest request) {
        InventoryItem item = findEntityForUpdate(id);
        int quantity=request.quantity();
        int reserved=safeInt(item.getReservedQuantity());

        if(reserved<quantity){
            throw new BusinessRuleException("Insufficiant reserved stock to release");
        }
        item.setReservedQuantity(reserved-quantity);
        item.setAvailableQuantity(safeInt(item.getAvailableQuantity())+quantity);
        return toResponse(repository.save(item));
    }

    @Override
    public InventoryResponse reserveStockByProductId(UUID productId, ReserveStockRequest request) {
      InventoryItem item=repository.findByProductIdForUpdate(productId)
              .orElseThrow(
                      ()->new ResourceNotFoundException("Inventory not found for ProductId: "+productId)
              );
        return reserve(item,request.quantity());
    }


    @Override
    public InventoryResponse releaseStockByProductId(UUID productId, ReleaseStockRequest request) {
        InventoryItem item=repository.findByProductIdForUpdate(productId)
                .orElseThrow(()->new ResourceNotFoundException("Inventory not found for productId: "+productId));
        return release(item,request.quantity());
    }

    @Override
    public void delete(Long id) {
        InventoryItem item =findEntity(id);
        repository.delete(item);
    }

    private InventoryResponse reserve(InventoryItem item,int quantity){
        int available=safeInt(item.getAvailableQuantity());
        if(available<quantity){
            throw  new BusinessRuleException("Insufficiant available storck to reserve");
        }
        item.setAvailableQuantity(available-quantity);
        item.setReservedQuantity(safeInt(item.getReservedQuantity())+quantity);
        return toResponse(repository.save(item));
    }

    private InventoryResponse release(InventoryItem item, int quantity){
        int reserved=safeInt(item.getReservedQuantity());
        if(reserved<quantity){
            throw new BusinessRuleException("Insufficiant reserved stock to release");
        }
        item.setReservedQuantity(reserved-quantity);
        item.setAvailableQuantity(safeInt(item.getAvailableQuantity())+quantity);
        return toResponse(repository.save(item));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private InventoryItem findEntityForUpdate(Long id) {
        return repository.findByIdForUpdate(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Inventory not found for id: " + id)
                );
    }

    private InventoryItem findEntity(Long id) {
        return repository
                .findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException
                                ("Inventory not found for given id: " + id));
    }


    private InventoryResponse toResponse(InventoryItem item) {

        return new InventoryResponse(
                item.getId(),
                item.getProductId(),
                item.getSku(),
                item.getProductName(),
                item.getWarehouseLocation(),
                item.getAvailableQuantity(),
                item.getReservedQuantity(),
                item.getReorderLevel(),
                item.isActive(),
                item.getTotalQuantity(),
                item.getLowStock(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private String normalizeSku(String sku) {
        if (!StringUtils.hasText(sku)) {
            throw new BusinessRuleException("SKU is required");
        }
        return sku.trim().toUpperCase();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
