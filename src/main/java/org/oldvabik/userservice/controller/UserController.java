package org.oldvabik.userservice.controller;

import jakarta.validation.Valid;
import org.oldvabik.userservice.dto.UserCreateDto;
import org.oldvabik.userservice.dto.UserDto;
import org.oldvabik.userservice.dto.UserUpdateDto;
import org.oldvabik.userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAnyRole('ADMIN') or #dto.email == authentication.name")
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateDto dto) {
        UserDto createdUser = userService.createUser(dto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(Authentication auth,
                                               @PathVariable Long id) {
        UserDto user = userService.getUserById(auth, id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserDto>> getUsers(@RequestParam(defaultValue = "0") Integer page,
                                                  @RequestParam(defaultValue = "5") Integer size) {
        Page<UserDto> users = userService.getAllUsers(page, size);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<UserDto> getUserByEmail(Authentication auth,
                                                  @RequestParam String email) {
        UserDto user = userService.getUserByEmail(auth, email);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(Authentication auth,
                                              @PathVariable Long id,
                                              @Valid @RequestBody UserUpdateDto dto) {
        UserDto updatedUser = userService.updateUser(auth, id, dto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}