package com.vd.easybuy.users.dto;

import com.vd.easybuy.users.entity.Role;

import java.util.UUID;

public record ChangeRoleRequest(
        UUID userId,
        Role role
) {
}
