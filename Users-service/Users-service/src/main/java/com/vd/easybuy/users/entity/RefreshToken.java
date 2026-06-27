package com.vd.easybuy.users.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "refresh_token")
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String refreshToken;

    @ManyToOne
    private User user;

    private Boolean active;
}
