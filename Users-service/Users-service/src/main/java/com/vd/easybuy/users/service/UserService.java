package com.vd.easybuy.users.service;

import com.vd.easybuy.users.dto.*;
import com.vd.easybuy.users.entity.Role;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDto createUser(UserDto userDto);
    LoginResponse login(LoginRequest loginRequest);
    TokenRefreshResponse refreshToken(TokenRefreshRequest refreshRequest);
    UserDto getUserById(UUID id);
    UserDto getUserByEmail(String email);
    List<UserDto> getAllUsers();
    UserDto updateUser(UUID id,UserDto userDto);
    void deleteUser(UUID id);
    void changeUserRole(UUID id, Role role);

}
