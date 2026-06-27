package com.vd.easybuy.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vd.easybuy.users.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserDto {
    private UUID id;
    private String name;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private Long phoneNumber;
    private String address;
    private Role role;
    private Instant createdAt;
    private Instant updatedAt;
}
