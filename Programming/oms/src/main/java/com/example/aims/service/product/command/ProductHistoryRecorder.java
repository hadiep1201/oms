package com.example.aims.service.product.command;

import com.example.aims.entity.History;
import com.example.aims.entity.Product;
import com.example.aims.entity.User;
import com.example.aims.exception.AppException;
import com.example.aims.exception.ErrorCode;
import com.example.aims.repository.HistoryRepository;
import com.example.aims.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * SRP: records product audit history only.
 * Coupling: data coupling with repositories.
 * Cohesion: functional cohesion.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductHistoryRecorder {

    UserRepository userRepository;
    HistoryRepository historyRepository;

    public void record(Product product, Integer userId, String action) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.USER_NOT_FOUND.getCode(),
                        ErrorCode.USER_NOT_FOUND.getMessage()));

        historyRepository.save(History.builder()
                .product(product)
                .createdByUser(user)
                .action(action)
                .timeStamp(Timestamp.from(Instant.now()))
                .build());
    }
}
