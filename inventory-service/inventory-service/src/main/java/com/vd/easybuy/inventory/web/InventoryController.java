package com.vd.easybuy.inventory.web;

import com.vd.easybuy.inventory.dto.*;
import com.vd.easybuy.inventory.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService){
        this.inventoryService=inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> create(@Valid @RequestBody CreateInventoryRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.create(request));
    }

    @PutMapping("/{id}")
    public InventoryResponse update(@PathVariable Long id, @Valid @RequestBody UpdateInventoryRequest request){
        return inventoryService.update(id,request);
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAll(){
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @GetMapping("/{id}")
    public InventoryResponse getById(@PathVariable Long id){
        return inventoryService.getById(id);
    }

    @GetMapping("/sku/{sku}")
    public InventoryResponse getBySku(@PathVariable String sku){
        return inventoryService.getBySku(sku);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStock(@RequestParam(defaultValue = "10") @Min(0) int threshold){
        return ResponseEntity.ok(inventoryService.getLowStock(threshold));
    }

    @PatchMapping("/{id}/adjust-stock")
    public ResponseEntity<InventoryResponse> adjustStock(@PathVariable Long id, @RequestBody AdjustStockRequest request){
        return ResponseEntity.ok(inventoryService.adjustStock(id,request));
    }

    @PostMapping("/{id}/reserve")
    public InventoryResponse reserve(@PathVariable Long id, @Valid @RequestBody ReserveStockRequest request){
        return inventoryService.reserveStock(id,request);
    }

    @PostMapping("/{id}/release")
    public InventoryResponse release(@PathVariable Long id, @Valid @RequestBody ReleaseStockRequest request){
        return inventoryService.releaseStock(id,request);
    }

    @PostMapping("/product/{productId}/reserve")
    public InventoryResponse reserveByProductId(@PathVariable UUID productId, @Valid @RequestBody ReserveStockRequest request) {
        return inventoryService.reserveStockByProductId(productId, request);
    }

    @PostMapping("/product/{productId}/release")
    public InventoryResponse releaseByProductId(@PathVariable UUID productId, @Valid @RequestBody ReleaseStockRequest request) {
        return inventoryService.releaseStockByProductId(productId, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
