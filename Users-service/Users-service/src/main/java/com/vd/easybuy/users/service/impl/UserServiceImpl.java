package com.vd.easybuy.users.service.impl;

import com.vd.easybuy.users.dto.UserDto;
import com.vd.easybuy.users.entity.Role;
import com.vd.easybuy.users.entity.User;
import com.vd.easybuy.users.exception.EmailAlreadyExistException;
import com.vd.easybuy.users.exception.InvalidRequestException;
import com.vd.easybuy.users.exception.ResourceNotFoundException;
import com.vd.easybuy.users.repository.UserRepository;
import com.vd.easybuy.users.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto createUser(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new InvalidRequestException("Email Alredy in use" + userDto.getEmail());
        }

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
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        User user = getUser(id);
        return toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email with user not exists" + email));
        return toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(UUID id, UserDto userDto) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found" + id));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new InvalidRequestException("Email alredy in use: " + userDto.getEmail());
            }
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getEmail() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getPassword() != null) {
            user.setPassword(userDto.getPassword());
        }
        if (userDto.getPhoneNumber() != null) {
            user.setPhoneNumber(userDto.getPhoneNumber());
        }

        if (userDto.getAddress() != null) {
            user.setAddress(userDto.getAddress());
        }
        if (userDto.getRole() != null) {
            user.setRole(Role.GUEST);
        }
        User updateUser = userRepository.save(user);
        return toDto(updateUser);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = getUser(id);
        userRepository.delete(user);
    }

    @Override
    public void changeUserRole(UUID id, Role role) {
        User user = getUser(id);
        user.setRole(role);
        User save = userRepository.save(user);
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
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());
        return userDto;
    }

    private User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with given id" + id));
    }

    private void alreadyExistEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistException("Email already in use");
        }
    }
}
