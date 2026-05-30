package com.vd.easybuy.products.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
//Declares a class which is not itself an entity, but whose mappings are inherited by the entities which extend it.
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(nullable = false,updatable = false)
    private Instant createdAt;
    @LastModifiedDate
    @Column(nullable=false)
    private Instant updatedAt;
}
