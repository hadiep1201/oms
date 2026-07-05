package com.example.aims.service;

import com.example.aims.entity.AdminLog;
import com.example.aims.entity.User;
import com.example.aims.repository.AdminLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminLogService {

    private final AdminLogRepository adminLogRepository;

    @Transactional
    public void logAction(User adminUser, User targetUser, String action) {
        AdminLog adminLog = AdminLog.builder()
                .adminUser(adminUser)
                .targetUser(targetUser)
                .action(action)
                .timeStamp(new Timestamp(System.currentTimeMillis()))
                .build();
        adminLogRepository.save(adminLog);
        log.info("AdminLog: Admin {} performed '{}' on User {}", adminUser.getEmail(), action, targetUser.getEmail());
    }
}
