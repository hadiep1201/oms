package com.example.aims.security;

import com.example.aims.entity.User;
import com.example.aims.entity.UserStatus;
import com.example.aims.exception.AimsException;
import com.example.aims.exception.ErrorCode;
import com.example.aims.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomUserDetailsService implements UserDetailsService {

    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return toPrincipal(findActiveUser(username));
    }

    public User findActiveUser(String username) {
        User user = userRepository.findByEmailIgnoreCase(username.trim())
                .or(() -> userRepository.findByUserNameIgnoreCase(username.trim()))
                .orElseThrow(() -> new AimsException(
                        ErrorCode.INVALID_CREDENTIALS.getCode(),
                        HttpStatus.UNAUTHORIZED,
                        ErrorCode.INVALID_CREDENTIALS.getMessage()));

        if (user.getStatus() != null && user.getStatus() != UserStatus.ACTIVE) {
            throw new AimsException(
                    ErrorCode.USER_INACTIVE.getCode(),
                    HttpStatus.FORBIDDEN,
                    ErrorCode.USER_INACTIVE.getMessage());
        }

        return user;
    }

    public UserPrincipal loadByUserId(Integer userId) {
        User user = userRepository.findWithRolesByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.getStatus() != null && user.getStatus() != UserStatus.ACTIVE) {
            throw new AimsException(
                    ErrorCode.USER_INACTIVE.getCode(),
                    HttpStatus.FORBIDDEN,
                    ErrorCode.USER_INACTIVE.getMessage());
        }
        return toPrincipal(user);
    }

    private UserPrincipal toPrincipal(User user) {
        return new UserPrincipal(user, userRepository.findRoleNamesByUserId(user.getUserId()));
    }

    public UserPrincipal loadPrincipal(User user) {
        return toPrincipal(user);
    }
}
