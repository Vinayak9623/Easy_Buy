package com.vd.easybuy.users.entity;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;


@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity{
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    @Column(nullable = false)
    private Long phoneNumber;
    private String address;
    @Enumerated(EnumType.STRING)
    private Role role=Role.GUEST;

}
