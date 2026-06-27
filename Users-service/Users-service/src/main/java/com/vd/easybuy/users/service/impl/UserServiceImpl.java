package com.vd.easybuy.users.service.impl;

import com.vd.easybuy.users.dto.*;
import com.vd.easybuy.users.entity.RefreshToken;
import com.vd.easybuy.users.entity.Role;
import com.vd.easybuy.users.entity.User;
import com.vd.easybuy.users.exception.EmailAlreadyExistException;
import com.vd.easybuy.users.exception.InvalidRequestException;
import com.vd.easybuy.users.exception.ResourceNotFoundException;
import com.vd.easybuy.users.repository.RefreshTokenRepo;
import com.vd.easybuy.users.repository.UserRepository;
import com.vd.easybuy.users.service.JwtService;
import com.vd.easybuy.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepo refreshTokenRepo;


    @Override
    public UserDto createUser(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new InvalidRequestException("Email Alredy in use" + userDto.getEmail());
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());
        user.setRole(Role.GUEST);
        User save = userRepository.save(user);
        return toDto(save);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login service started");

       User user =userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()->new InvalidRequestException("Invalid email"));

        if(!passwordEncoder.matches(loginRequest.getPassword(),user.getPassword())){
            throw new InvalidRequestException("Invalid Email or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        var token=new RefreshToken();
        token.setRefreshToken(refreshToken);
        token.setActive(true);
        token.setUser(user);
        refreshTokenRepo.save(token);

        LoginResponse loginResponse=new LoginResponse();

        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setUser(toDto(user));

        log.info("Login service executed");

        return loginResponse;
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String email = jwtService.extractUsername(refreshToken);

        if(!jwtService.getTokenType(refreshToken).equals("refresh_token")){
            throw new InvalidRequestException("Invalid refresh token");
        }

        RefreshToken token = refreshTokenRepo.findByRefreshToken(refreshToken).orElseThrow(
                () -> new InvalidRequestException("Invalid refresh token"));

        if(!token.getActive()){
            throw new InvalidRequestException("invalid refresh token");
        }

        User user=userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found for given refresh token"));

        if(!jwtService.isTokenValid(refreshToken, user.getEmail())){
            throw new InvalidRequestException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

        token.setActive(false);
        refreshTokenRepo.save(token);
        var refreshTokenOb1 = new RefreshToken();
        refreshTokenOb1.setRefreshToken(newRefreshToken);
        refreshTokenOb1.setActive(true);
        refreshTokenOb1.setUser(user);
        refreshTokenRepo.save(refreshTokenOb1);


        TokenRefreshResponse refreshResponse = new TokenRefreshResponse();
        refreshResponse.setAccessToken(newAccessToken);
        refreshResponse.setRefreshToken(newRefreshToken);

        return refreshResponse;
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
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
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
