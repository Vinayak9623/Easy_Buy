package com.vd.easybuy.users.service;

import com.vd.easybuy.users.dto.UserDto;
import com.vd.easybuy.users.entity.Role;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDto registerUser(UserDto userDto);
    UserDto getUserById(UUID id);
    UserDto getUserByEmail(String email);
    List<UserDto> getAllUser();
    UserDto updateUser(UUID id,UserDto userDto);
    void deleteUser(UUID id);
    UserDto changeUserRole(UUID id, Role role);

}
