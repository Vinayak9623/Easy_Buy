package com.vd.easybuy.products.repository;

import com.vd.easybuy.products.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryRepo extends JpaRepository<Category,Long> {

    @Query("SELECT c from Category c join c.products p where p.id=:productId")
    List<Category> findByProductId(@Param("productId") UUID productId);
}
