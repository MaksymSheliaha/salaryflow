package com.msk.salaryflow.aspect;

import com.msk.salaryflow.aspect.annotation.LogEvent;
import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.entity.Department;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.entity.EventLog;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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

        // 1. Пошук старої версії
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

        // 2. Виконання методу
        Object result = joinPoint.proceed();

        // 3. Обробка результату
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

                // Отримуємо гарне ім'я
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
    public void saveLogAsync(String action, String entity, String user, String targetId, String targetName, Map<String, String> changes) {
        try {
            EventLog log = new EventLog(action, entity, user, targetId, targetName, changes);
            eventLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    // --- Допоміжні методи ---

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

                if (fieldName.equals("roles") || fieldName.equals("password")
                        || fieldName.equals("employee") || fieldName.equals("department")
                        || java.util.Collection.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                Object oldVal = field.get(oldObj);
                Object newVal = field.get(newObj);

                if (oldVal == null && newVal == null) continue;

                if (!Objects.equals(oldVal, newVal)) {
                    String oldS = (oldVal != null) ? oldVal.toString() : "null";
                    String newS = (newVal != null) ? newVal.toString() : "null";

                    if (oldS.contains("T00:00")) oldS = oldS.split("T")[0];
                    if (newS.contains("T00:00")) newS = newS.split("T")[0];

                    diff.put(fieldName, oldS + " -> " + newS);
                }
            }
        } catch (Exception e) {
            System.err.println("Error calculating diff: " + e.getMessage());
        }
        return diff;
    }

    // --- ОСНОВНА ЗМІНА ТУТ ---
    private String getNiceName(Object obj) {
        try {
            // 1. Для працівника - все просто
            if (obj instanceof Employee) {
                Employee e = (Employee) obj;
                return e.getFirstName() + " " + e.getLastName();
            }

            // 2. Для відсутності - ТУТ БУЛА ПРОБЛЕМА
            if (obj instanceof Absence) {
                Absence a = (Absence) obj;
                String empName = "Unknown";

                // Спершу пробуємо взяти з об'єкта
                if (a.getEmployee() != null && a.getEmployee().getFirstName() != null) {
                    empName = a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName();
                }
                // Якщо там пусто (null), але є ID - шукаємо в базі через EntityManager
                else if (a.getEmployeeId() != null) {
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

            // 3. Для департаменту
            if (obj instanceof Department) {
                return ((Department) obj).getName();
            }

            // 4. Фолбек
            Field nameField = null;
            try { nameField = obj.getClass().getDeclaredField("name"); } catch (NoSuchFieldException e) {
                try { nameField = obj.getClass().getDeclaredField("firstName"); } catch (NoSuchFieldException ignored) {}
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