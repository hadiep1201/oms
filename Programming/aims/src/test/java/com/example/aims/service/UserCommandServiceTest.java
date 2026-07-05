package com.example.aims.service;

import com.example.aims.dto.request.CreateUserRequest;
import com.example.aims.dto.request.UpdateUserRequest;
import com.example.aims.dto.response.UserResponse;
import com.example.aims.entity.User;
import com.example.aims.entity.UserStatus;
import com.example.aims.exception.AimsException;
import com.example.aims.repository.UserRepository;
import com.example.aims.service.notification.EmailNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminLogService adminLogService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private UserCommandService userCommandService;

    private User adminUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setUserId(1);
        adminUser.setUserName("Admin");
        adminUser.setEmail("admin@aims.com");
        adminUser.setStatus(UserStatus.ACTIVE);

        targetUser = new User();
        targetUser.setUserId(2);
        targetUser.setUserName("Test User 1");
        targetUser.setEmail("testuser1@example.com");
        targetUser.setStatus(UserStatus.ACTIVE);
    }

    // UT070: Create User - Normal (Happy Path)
    @Test
    void testCreateUser_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("New User");
        request.setEmail("newuser@example.com");
        request.setPassword("Password@123");

        when(userRepository.findByEmailIgnoreCase("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setUserId(3);
        savedUser.setUserName("New User");
        savedUser.setEmail("newuser@example.com");
        savedUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userCommandService.createUser(request, 1);

        assertNotNull(response);
        assertEquals("New User", response.getUserName());
        assertEquals("newuser@example.com", response.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
        verify(adminLogService, times(1)).logAction(eq(adminUser), any(User.class), eq("CREATE_USER"));
    }

    // UT070_Duplicate: Create User - Email already exists
    @Test
    void testCreateUser_EmailConflict() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("Another Admin");
        request.setEmail("admin@aims.com");
        request.setPassword("Pass@123");

        when(userRepository.findByEmailIgnoreCase("admin@aims.com")).thenReturn(Optional.of(adminUser));

        AimsException exception = assertThrows(AimsException.class, () -> {
            userCommandService.createUser(request, 1);
        });

        assertEquals("Email already exists in the system", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // UT073: Update User Basic Information - Normal
    @Test
    void testUpdateUserBasicInfo_Success_NoEmailChange() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUserName("Updated Name");
        request.setEmail("testuser1@example.com"); // Same email

        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        UserResponse response = userCommandService.updateUserBasicInfo(2, request, 1);

        assertNotNull(response);
        assertEquals("Updated Name", targetUser.getUserName());
        
        verify(adminLogService, times(1)).logAction(eq(adminUser), eq(targetUser), eq("UPDATE_USER_BASIC_INFO"));
        verify(emailNotificationService, never()).sendEmailUpdateNotification(anyString(), anyString(), anyString());
    }
    
    // UT074: Update User Basic Information - Email Conflict
    @Test
    void testUpdateUserBasicInfo_EmailConflict() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUserName("Updated Name");
        request.setEmail("admin@aims.com"); // Email already belongs to admin

        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2)).thenReturn(Optional.of(targetUser));
        when(userRepository.findByEmailIgnoreCase("admin@aims.com")).thenReturn(Optional.of(adminUser));

        AimsException exception = assertThrows(AimsException.class, () -> {
            userCommandService.updateUserBasicInfo(2, request, 1);
        });

        assertEquals("Email already exists in the system", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    // UT074_EmailTrigger: Update User Basic Information - Email changed successfully
    @Test
    void testUpdateUserBasicInfo_Success_EmailChange() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUserName("Updated Name");
        request.setEmail("newemail@example.com"); 

        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2)).thenReturn(Optional.of(targetUser));
        when(userRepository.findByEmailIgnoreCase("newemail@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        userCommandService.updateUserBasicInfo(2, request, 1);
        
        verify(adminLogService, times(1)).logAction(eq(adminUser), eq(targetUser), eq("UPDATE_USER_EMAIL"));
        verify(emailNotificationService, times(1)).sendEmailUpdateNotification(eq("testuser1@example.com"), eq("newemail@example.com"), eq("Updated Name"));
    }

    // UT075: Deactivate User - Normal
    @Test
    void testDeactivateUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2)).thenReturn(Optional.of(targetUser));

        userCommandService.deactivateUser(2, 1);

        assertEquals(UserStatus.DEACTIVATED, targetUser.getStatus());
        verify(userRepository, times(1)).save(targetUser);
        verify(adminLogService, times(1)).logAction(eq(adminUser), eq(targetUser), eq("DEACTIVATE_USER"));
    }
    
    // UT076: Deactivate User - Prevent Self-Deactivation
    @Test
    void testDeactivateUser_SelfDeactivation() {
        AimsException exception = assertThrows(AimsException.class, () -> {
            userCommandService.deactivateUser(1, 1);
        });

        assertEquals("Admin cannot deactivate their own account", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    // UT077: Deactivate User - Already Deactivated
    @Test
    void testDeactivateUser_AlreadyDeactivated() {
        targetUser.setStatus(UserStatus.DEACTIVATED);
        
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2)).thenReturn(Optional.of(targetUser));

        AimsException exception = assertThrows(AimsException.class, () -> {
            userCommandService.deactivateUser(2, 1);
        });

        assertEquals("User is already deactivated", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
