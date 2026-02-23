package com.animalfarm.service;

import com.animalfarm.auth.AuthSession;
import com.animalfarm.model.AuditLog;
import com.animalfarm.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(AuthSession session, String action, String details) {
        AuditLog log = new AuditLog();
        log.setActorUserId(session.userId());
        log.setActorUsername(session.username());
        log.setActorRole(session.role());
        log.setAction(action);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
