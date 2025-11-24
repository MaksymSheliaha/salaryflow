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
        String entityName = joinPoint.getSignature().getDeclaringType().getSimpleName();

        String currentUser = getCurrentUsername();

        saveLogAsync(action, entityName, currentUser, result);
    }

    @Async
    public void saveLogAsync(String action, String entity, String user, Object payload) {
        try {
            EventLog log = new EventLog(action, entity, user, payload);
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