package com.vd.easybuy.products.repository;

import com.vd.easybuy.products.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReviewRepo extends JpaRepository<Review,Long> {

    @Query("SELECT r from Review r join r.product p where p.id=:productId")
    List<Review> findByProduct_Id(@Param("productId") UUID productId);
}
