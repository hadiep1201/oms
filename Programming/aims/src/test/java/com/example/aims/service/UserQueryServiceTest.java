package com.example.aims.service;

import com.example.aims.dto.response.UserResponse;
import com.example.aims.entity.User;
import com.example.aims.entity.UserStatus;
import com.example.aims.exception.ResourceNotFoundException;
import com.example.aims.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserQueryService userQueryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(2);
        testUser.setUserName("Test User 1");
        testUser.setEmail("testuser1@example.com");
        testUser.setStatus(UserStatus.ACTIVE);
    }

    // UT071: View User Basic Information - Normal (Happy Path)
    @Test
    void testGetUserDetails_Success() {
        when(userRepository.findById(2)).thenReturn(Optional.of(testUser));

        UserResponse response = userQueryService.getUserDetails(2);

        assertNotNull(response);
        assertEquals(2, response.getUserId());
        assertEquals("Test User 1", response.getUserName());
        assertEquals("testuser1@example.com", response.getEmail());
        assertEquals("ACTIVE", response.getStatus());

        verify(userRepository, times(1)).findById(2);
    }

    // UT072: View User Basic Information - Error (User Not Found)
    @Test
    void testGetUserDetails_UserNotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userQueryService.getUserDetails(999);
        });

        assertTrue(exception.getMessage().contains("Cannot find user with id: 999"));

        verify(userRepository, times(1)).findById(999);
    }
}
