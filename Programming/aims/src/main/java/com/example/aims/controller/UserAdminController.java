package com.example.aims.controller;

import com.example.aims.dto.request.CreateUserRequest;
import com.example.aims.dto.request.UpdateUserRequest;
import com.example.aims.dto.response.UserResponse;
import com.example.aims.service.UserCommandService;
import com.example.aims.service.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Assuming standard cross-origin for dev
public class UserAdminController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    // adminId is now extracted from SecurityContextHolder

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userQueryService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userQueryService.getUserDetails(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        Integer adminId = com.example.aims.security.SecurityUtils.requireCurrentUserId();
        return new ResponseEntity<>(userCommandService.createUser(request, adminId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Integer id, @Valid @RequestBody UpdateUserRequest request) {
        Integer adminId = com.example.aims.security.SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(userCommandService.updateUserBasicInfo(id, request, adminId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Integer id) {
        Integer adminId = com.example.aims.security.SecurityUtils.requireCurrentUserId();
        userCommandService.deactivateUser(id, adminId);
        return ResponseEntity.noContent().build();
    }
}
