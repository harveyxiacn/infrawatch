package com.infrawatch.service.auth;

import com.infrawatch.model.auth.AuditLog;
import com.infrawatch.model.auth.User;
import com.infrawatch.repository.auth.AuditLogRepository;
import com.infrawatch.repository.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public void log(String action, String entityType, UUID entityId, String details) {
        String username = getCurrentUsername();
        UUID userId = userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "system";
        }
        return authentication.getName();
    }
}
