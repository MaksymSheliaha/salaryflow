package com.msk.salaryflow.aspect;

import com.msk.salaryflow.aspect.annotation.LogEvent;
import com.msk.salaryflow.entity.*;
import com.msk.salaryflow.repository.EventLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final EventLogRepository eventLogRepository;
    private final EntityManager entityManager;

    @Around("@annotation(logEvent)")
    public Object logAround(ProceedingJoinPoint joinPoint, LogEvent logEvent) throws Throwable {
        String action = logEvent.action();
        Object[] args = joinPoint.getArgs();

        Object oldEntity = null;
        if (action.startsWith("UPDATE") && args.length > 0) {
            try {
                Object newEntityArg = args[0];
                UUID id = getIdFromEntity(newEntityArg);
                if (id != null) {
                    oldEntity = entityManager.find(newEntityArg.getClass(), id);
                    if (oldEntity != null) {
                        entityManager.detach(oldEntity);
                    }
                }
            } catch (Exception e) {
                System.err.println("Audit Log Error (Pre-fetch): " + e.getMessage());
            }
        }

        Object result = joinPoint.proceed();

        try {
            String currentUser = getCurrentUsername();
            String entityName = "Unknown";
            String targetId = null;
            String targetName = null;
            Map<String, String> changes = null;


            Object targetEntity = (result != null) ? result : (args.length > 0 ? args[0] : null);

            if (targetEntity != null) {
                entityName = targetEntity.getClass().getSimpleName();
                UUID id = getIdFromEntity(targetEntity);
                targetId = (id != null) ? id.toString() : null;

                targetName = getNiceName(targetEntity);

                if (action.startsWith("UPDATE") && oldEntity != null) {
                    changes = calculateDiff(oldEntity, targetEntity);
                }
            }

            if (changes == null || changes.isEmpty()) {
                changes = null;
            }

            saveLogAsync(action, entityName, currentUser, targetId, targetName, changes);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Async
    public void saveLogAsync(String action, String entity, String user, String targetId, String targetName,
                             Map<String, String> changes) {
        try {
            EventLog log = new EventLog(action, entity, user, targetId, targetName, changes);
            eventLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    private UUID getIdFromEntity(Object entity) {
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class) || field.getName().equals("id")) {
                    field.setAccessible(true);
                    return (UUID) field.get(entity);
                }
            }
            Class<?> superclass = entity.getClass().getSuperclass();
            while (superclass != null) {
                for (Field field : superclass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Id.class) || field.getName().equals("id")) {
                        field.setAccessible(true);
                        return (UUID) field.get(entity);
                    }
                }
                superclass = superclass.getSuperclass();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Map<String, String> calculateDiff(Object oldObj, Object newObj) {
        Map<String, String> diff = new HashMap<>();
        try {
            for (Field field : oldObj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();

                if (fieldName.equals("password")
                        || fieldName.equals("employee") || fieldName.equals("department")
                        || fieldName.equals("authorities")) {
                    continue;
                }

                Object oldVal = field.get(oldObj);
                Object newVal = field.get(newObj);

                if (oldVal == null && newVal == null) continue;

                if (fieldName.equals("roles")) {
                    String oldRoles = rolesToString(oldVal);
                    String newRoles = rolesToString(newVal);
                    if (!oldRoles.equals(newRoles)) {
                        diff.put("roles", oldRoles + " -> " + newRoles);
                    }
                    continue;
                }

                if (!Objects.equals(oldVal, newVal)) {
                    String oldS = (oldVal != null) ? oldVal.toString() : "null";
                    String newS = (newVal != null) ? newVal.toString() : "null";

                    if (oldS.contains("T00:00")) oldS = oldS.split("T")[0];
                    if (newS.contains("T00:00")) newS = newS.split("T")[0];

                    if (fieldName.equals("enabled")) {
                        if(oldS.equals("true")) oldS = "Active"; else oldS = "Banned";
                        if(newS.equals("true")) newS = "Active"; else newS = "Banned";
                    }

                    diff.put(fieldName, oldS + " -> " + newS);
                }
            }
        } catch (Exception e) {
            System.err.println("Error calculating diff: " + e.getMessage());
        }
        return diff;
    }

    private String rolesToString(Object roleCollection) {
        if (roleCollection instanceof Collection<?>) {
            return ((Collection<?>) roleCollection).stream()
                    .map(obj -> {
                        if (obj instanceof Role) return ((Role) obj).getName();
                        return obj.toString();
                    })
                    .sorted()
                    .collect(Collectors.joining(", ", "[", "]"));
        }
        return "[]";
    }

    private String getNiceName(Object obj) {
        try {
            if (obj instanceof Employee) {
                Employee e = (Employee) obj;
                return e.getFirstName() + " " + e.getLastName();
            }

            if (obj instanceof User) {
                User u = (User) obj;
                String status = u.isEnabled() ? "Active" : "Banned";
                return u.getUsername() + " (" + status + ")";
            }

            if (obj instanceof Department) {
                return ((Department) obj).getName();
            }

            if (obj instanceof Absence) {
                Absence a = (Absence) obj;
                String empName = "Unknown";
                if (a.getEmployee() != null && a.getEmployee().getFirstName() != null) {
                    empName = a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName();
                } else if (a.getEmployeeId() != null) {
                    try {
                        Employee e = entityManager.find(Employee.class, a.getEmployeeId());
                        if (e != null) {
                            empName = e.getFirstName() + " " + e.getLastName();
                        } else {
                            empName = "Deleted Employee";
                        }
                    } catch (Exception ex) {
                        empName = "Employee#" + a.getEmployeeId();
                    }
                }
                return empName + " (" + a.getType() + ")";
            }

            Field nameField = null;
            try { nameField = obj.getClass().getDeclaredField("name"); } catch (NoSuchFieldException e) {
                try { nameField = obj.getClass().getDeclaredField("firstName"); } catch (NoSuchFieldException ignored) {}
                try { nameField = obj.getClass().getDeclaredField("username"); } catch (NoSuchFieldException ignored) {}
            }

            if (nameField != null) {
                nameField.setAccessible(true);
                Object val = nameField.get(obj);
                return val != null ? val.toString() : obj.toString();
            }

        } catch (Exception ignored) {}
        return "ID: " + getIdFromEntity(obj);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "unknown";
    }
}