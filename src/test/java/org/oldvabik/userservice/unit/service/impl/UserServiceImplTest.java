package org.oldvabik.userservice.unit.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oldvabik.userservice.dto.*;
import org.oldvabik.userservice.entity.User;
import org.oldvabik.userservice.exception.*;
import org.oldvabik.userservice.mapper.UserMapper;
import org.oldvabik.userservice.repository.UserRepository;
import org.oldvabik.userservice.security.AccessChecker;
import org.oldvabik.userservice.service.impl.UserServiceImpl;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AccessChecker accessChecker;
    @Mock
    private Authentication auth;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_success() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("test@example.com");
        User user = new User();
        User savedUser = new User();
        savedUser.setId(1L);
        UserDto userDto = new UserDto();

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toEntity(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(userDto);

        UserDto result = userService.createUser(dto);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_emailExists_throwsException() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("exists@example.com");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(AlreadyExistsException.class, () -> userService.createUser(dto));
    }

    @Test
    void getUserById_found() {
        User user = new User();
        user.setEmail("test@example.com");
        UserDto dto = new UserDto();
        dto.setEmail("test@example.com");

        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);
        when(accessChecker.canAccessUser(any(Authentication.class), any(UserDto.class))).thenReturn(true);

        UserDto result = userService.getUserById(auth, 1L);
        assertNotNull(result);
    }

    @Test
    void getUserById_accessDenied() {
        User user = new User();
        user.setEmail("other@example.com");
        UserDto dto = new UserDto();
        dto.setEmail("other@example.com");

        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);
        when(accessChecker.canAccessUser(any(Authentication.class), any(UserDto.class))).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> userService.getUserById(auth, 1L));
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserById(auth, 1L));
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAllWithCards(any())).thenReturn(new PageImpl<>(List.of(new User())));
        when(userMapper.toDto(any())).thenReturn(new UserDto());

        var result = userService.getAllUsers(0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getUserByEmail_found() {
        User user = new User();
        user.setEmail("test@example.com");
        UserDto dto = new UserDto();
        dto.setEmail("test@example.com");

        when(userRepository.findByEmailWithCards("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);
        when(accessChecker.canAccessUser(any(Authentication.class), any(UserDto.class))).thenReturn(true);

        UserDto result = userService.getUserByEmail(auth, "test@example.com");
        assertNotNull(result);
    }

    @Test
    void getUserByEmail_accessDenied() {
        User user = new User();
        user.setEmail("other@example.com");
        UserDto dto = new UserDto();
        dto.setEmail("other@example.com");

        when(userRepository.findByEmailWithCards("other@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);
        when(accessChecker.canAccessUser(any(Authentication.class), any(UserDto.class))).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> userService.getUserByEmail(auth, "other@example.com"));
    }

    @Test
    void getUserByEmail_notFound() {
        when(userRepository.findByEmailWithCards("email@test.com")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserByEmail(auth, "email@test.com"));
    }

    @Test
    void updateUser_success() {
        Long id = 1L;
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("NewName");
        dto.setSurname("NewSurname");
        User user = new User();
        user.setId(id);
        UserDto dtoUser = new UserDto();
        User savedUser = new User();
        savedUser.setId(id);
        UserDto userDto = new UserDto();

        when(userRepository.findByIdWithCards(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dtoUser);
        when(accessChecker.canAccessUser(any(Authentication.class), any(UserDto.class))).thenReturn(true);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(userDto);

        UserDto result = userService.updateUser(auth, id, dto);
        assertNotNull(result);
    }

    @Test
    void updateUser_accessDenied() {
        Long id = 1L;
        UserUpdateDto dto = new UserUpdateDto();
        User user = new User();
        user.setId(id);
        UserDto dtoUser = new UserDto();

        when(userRepository.findByIdWithCards(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dtoUser);
        when(accessChecker.canAccessUser(any(Authentication.class), any(UserDto.class))).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> userService.updateUser(auth, id, dto));
    }

    @Test
    void deleteUser_success() {
        Long userId = 1L;
        String email = "test@example.com";

        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(redisTemplate.keys("users::1_*"))
                .thenReturn(Set.of("users::1_admin", "users::1_user"));

        when(redisTemplate.keys("users::test@example.com_*"))
                .thenReturn(Set.of("users::test@example.com_admin"));

        when(redisTemplate.delete(anyCollection())).thenReturn(2L);

        userService.deleteUser(userId);

        verify(userRepository).delete(user);

        verify(redisTemplate, times(2)).delete(anyCollection());

        verify(redisTemplate).delete(Set.of("users::1_admin", "users::1_user"));
        verify(redisTemplate).delete(Set.of("users::test@example.com_admin"));
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
    }
}
