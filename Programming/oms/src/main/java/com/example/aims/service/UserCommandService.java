package com.example.aims.service;

import com.example.aims.dto.request.CreateUserRequest;
import com.example.aims.dto.request.UpdateUserRequest;
import com.example.aims.dto.response.UserResponse;
import com.example.aims.entity.User;
import com.example.aims.entity.UserStatus;
import com.example.aims.exception.ResourceNotFoundException;
import com.example.aims.repository.UserRepository;
import com.example.aims.service.notification.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminLogService adminLogService;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    public UserResponse createUser(CreateUserRequest request, Integer adminId) {
        if (userRepository.findByEmailIgnoreCase(request.getEmail().trim()).isPresent()) {
            throw new com.example.aims.exception.AimsException(
                com.example.aims.exception.ErrorCode.INVALID_REQUEST.getCode(),
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Email already exists in the system"
            );
        }
        
        User admin = getUserById(adminId);
        
        User newUser = User.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .avatarUrl(request.getAvatarUrl())
                .status(UserStatus.ACTIVE)
                .createdByUser(admin)
                .build();
                
        userRepository.save(newUser);
        
        adminLogService.logAction(admin, newUser, "CREATE_USER");
        
        return mapToResponse(newUser);
    }

    @Transactional
    public UserResponse updateUserBasicInfo(Integer userId, UpdateUserRequest request, Integer adminId) {
        User admin = getUserById(adminId);
        User user = getUserById(userId);
        
        if (UserStatus.DEACTIVATED.equals(user.getStatus())) {
            throw new com.example.aims.exception.AimsException(
                com.example.aims.exception.ErrorCode.INVALID_REQUEST.getCode(),
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Cannot update a deactivated user"
            );
        }
        
        String oldEmail = user.getEmail();
        boolean emailChanged = !oldEmail.equalsIgnoreCase(request.getEmail().trim());
        
        if (emailChanged && userRepository.findByEmailIgnoreCase(request.getEmail().trim()).isPresent()) {
            throw new com.example.aims.exception.AimsException(
                com.example.aims.exception.ErrorCode.INVALID_REQUEST.getCode(),
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Email already exists in the system"
            );
        }
        
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setAvatarUrl(request.getAvatarUrl());
        
        userRepository.save(user);
        
        adminLogService.logAction(admin, user, "UPDATE_USER_BASIC_INFO");
        
        if (emailChanged) {
            adminLogService.logAction(admin, user, "UPDATE_USER_EMAIL");
            emailNotificationService.sendEmailUpdateNotification(oldEmail, request.getEmail(), user.getUserName());
        }
        
        return mapToResponse(user);
    }

    @Transactional
    public void deactivateUser(Integer userId, Integer adminId) {
        if (userId.equals(adminId)) {
            throw new com.example.aims.exception.AimsException(
                com.example.aims.exception.ErrorCode.INVALID_REQUEST.getCode(),
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Admin cannot deactivate their own account"
            );
        }

        User admin = getUserById(adminId);
        User user = getUserById(userId);
        
        if (UserStatus.DEACTIVATED.equals(user.getStatus())) {
            throw new com.example.aims.exception.AimsException(
                com.example.aims.exception.ErrorCode.INVALID_REQUEST.getCode(),
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "User is already deactivated"
            );
        }
        
        user.setStatus(UserStatus.DEACTIVATED);
        userRepository.save(user);
        
        adminLogService.logAction(admin, user, "DEACTIVATE_USER");
    }

    private User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find user with id: " + userId));
    }
    
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .build();
    }
}
