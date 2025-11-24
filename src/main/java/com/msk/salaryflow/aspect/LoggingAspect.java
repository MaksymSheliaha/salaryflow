package com.msk.salaryflow.aspect;

import com.msk.salaryflow.aspect.annotation.LogEvent;
import com.msk.salaryflow.entity.EventLog;
import com.msk.salaryflow.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final EventLogRepository eventLogRepository;


    @AfterReturning(pointcut = "@annotation(logEvent)", returning = "result")
    public void logAfterMethod(JoinPoint joinPoint, LogEvent logEvent, Object result) {
        String action = logEvent.action();
        String entityName = result != null ? result.getClass().getSimpleName() : "Unknown";


        String currentUser = getCurrentUsername();


        String targetId = null;
        if (result != null) {
            try {
                var idField = result.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                Object idValue = idField.get(result);
                targetId = (idValue != null) ? idValue.toString() : null;
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }

        saveLogAsync(action, entityName, currentUser, targetId);
    }

    @Async
    public void saveLogAsync(String action, String entity, String user, String targetId) {
        try {
            EventLog log = new EventLog(action, entity, user, targetId);
            eventLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to write audit log to MongoDB: " + e.getMessage());
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            if (!"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        }

        return "unknown";
    }
}