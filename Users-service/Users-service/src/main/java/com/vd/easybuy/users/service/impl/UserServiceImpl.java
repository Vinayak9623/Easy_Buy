package com.vd.easybuy.users.service.impl;

import com.vd.easybuy.users.dto.UserDto;
import com.vd.easybuy.users.entity.Role;
import com.vd.easybuy.users.entity.User;
import com.vd.easybuy.users.exception.EmailAlreadyExistException;
import com.vd.easybuy.users.repository.UserRepository;
import com.vd.easybuy.users.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto registerUser(UserDto userDto) {
        alreadyExistEmail(userDto.getEmail());
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());//it should be encoded
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());
        user.setRole(Role.GUEST);
        User save = userRepository.save(user);
        return toDto(save);
    }

    @Override
    public UserDto getUserById(UUID id) {
        User user = getUser(id);
        return toDto(user);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Email with user not exists"));
        return toDto(user);
    }

    @Override
    public List<UserDto> getAllUser() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::toDto).toList();
    }

    @Override
    public UserDto updateUser(UUID id, UserDto userDto) {
        User user = getUser(id);
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());//it should be encoded
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());
        User save = userRepository.save(user);
        return toDto(save);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = getUser(id);
        userRepository.deleteById(id);
    }

    @Override
    public UserDto changeUserRole(UUID id, Role role) {
        User user = getUser(id);
        user.setRole(role);
        User save = userRepository.save(user);
        return toDto(save);
    }

    private UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());//it should be encoded
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setAddress(user.getAddress());
        userDto.setRole(user.getRole());
        return userDto;
    }

    private User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with given id"));
    }

    private void alreadyExistEmail(String email) {
            if(userRepository.existsByEmail(email)){
                throw new EmailAlreadyExistException("Email already in use");
            }
    }
}
