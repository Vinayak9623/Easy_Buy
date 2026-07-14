package com.vd.easybuy.users.service;

import com.vd.easybuy.users.dto.UserDto;
import com.vd.easybuy.users.entity.User;
import com.vd.easybuy.users.exception.InvalidRequestException;
import com.vd.easybuy.users.repository.RefreshTokenRepo;
import com.vd.easybuy.users.repository.UserRepository;
import com.vd.easybuy.users.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepo refreshTokenRepo;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldThrowException_whenEmailAlreadyExist(){
     //arrange
        UserDto userDto=new UserDto();
        userDto.setEmail("vinayak@gmail.com");

        Mockito.when(userRepository.existsByEmail("vinayak@gmail.com")).thenReturn(true);
        //act and assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.createUser(userDto));
        assertEquals("Email Alredy in usevinayak@gmail.com",exception.getMessage());
        Mockito.verify(userRepository,Mockito.never()).save(Mockito.any(User.class));

    }
}
