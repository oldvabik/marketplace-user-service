package org.oldvabik.userservice.service;

import org.oldvabik.userservice.dto.UserCreateDto;
import org.oldvabik.userservice.dto.UserDto;
import org.oldvabik.userservice.dto.UserUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

public interface UserService {
    UserDto createUser(UserCreateDto dto);

    UserDto getUserById(Authentication auth, Long id);

    Page<UserDto> getAllUsers(Integer page, Integer size);

    UserDto getUserByEmail(Authentication auth, String email);

    UserDto updateUser(Authentication auth, Long id, UserUpdateDto dto);

    void deleteUser(Long id);
}
