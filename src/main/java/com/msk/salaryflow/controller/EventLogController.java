package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.EventLog;
import com.msk.salaryflow.service.*; // Імпортуємо всі сервіси
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("events")
public class EventLogController {

    private final EventLogService eventLogService;

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final AbsenceService absenceService;
    private final UserService userService;

    @GetMapping
    public String listEvents(@RequestParam(required = false) String from,
                             @RequestParam(required = false) String to,
                             @RequestParam(required = false) String eventType,
                             @RequestParam(required = false) String entity,
                             @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
                             Model model) {

        if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate = LocalDate.parse(to);

            if (fromDate.isAfter(toDate)) {
                model.addAttribute("error", "Start date cannot be after End date.");

                model.addAttribute("eventLogs", Page.empty().getContent());
                model.addAttribute("page", Page.empty(pageable));

                model.addAttribute("from", from);
                model.addAttribute("to", to);
                model.addAttribute("eventType", eventType);
                model.addAttribute("entity", entity);

                return "events/event-log-list";
            }
        }

        Page<EventLog> eventLogs = eventLogService.getEventLogs(pageable, from, to, eventType, entity);

        model.addAttribute("eventLogs", eventLogs.getContent());
        model.addAttribute("page", eventLogs);

        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("eventType", eventType);
        model.addAttribute("entity", entity);

        Sort.Order sortOrder = pageable.getSort().stream().findFirst().orElse(null);
        model.addAttribute("sortField", sortOrder != null ? sortOrder.getProperty() : "timestamp");
        model.addAttribute("sortDir", sortOrder != null ? sortOrder.getDirection().name().toLowerCase() : "desc");

        return "events/event-log-list";
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable UUID id,
                              @RequestParam(required = false) String from,
                              @RequestParam(required = false) String to,
                              Pageable pageable) {
        eventLogService.deleteById(id);
        return "redirect:/events?from=" + (from != null ? from : "") + "&to=" + (to != null ? to : "") + "&page=" + pageable.getPageNumber();
    }

    @PostMapping("/delete-by-filter")
    public String deleteByFilter(@RequestParam(required = false) String from,
                                 @RequestParam(required = false) String to,
                                 @RequestParam(required = false) String eventType,
                                 @RequestParam(required = false) String entity) {

        eventLogService.deleteByFilter(from, to, eventType, entity);

        return "redirect:/events?from=" + (from != null ? from : "") +
                "&to=" + (to != null ? to : "") +
                "&eventType=" + (eventType != null ? eventType : "") +
                "&entity=" + (entity != null ? entity : "");
    }

    @PostMapping("/delete-all")
    public String deleteAll() {
        eventLogService.deleteAll();
        return "redirect:/events";
    }

    @GetMapping("/open-target")
    public String openTarget(@RequestParam("entity") String entityName,
                             @RequestParam("targetId") String targetId,
                             @RequestParam("action") String action,
                             @RequestParam("logId") UUID logId) {

        if (action != null && action.toUpperCase().startsWith("DELETE")) {
            return "redirect:/events/" + logId + "/raw";
        }

        boolean exists = false;
        String redirectUrl = "/";

        try {
            UUID uuid = UUID.fromString(targetId);

            switch (entityName) {
                case "Employee":
                    exists = employeeService.findById(uuid) != null;
                    redirectUrl = "/employees/" + targetId;
                    break;
                case "Department":
                    exists = departmentService.findById(uuid) != null;
                    redirectUrl = "/departments/" + targetId;
                    break;
                case "Absence":
                    exists = absenceService.findById(uuid).isPresent(); // absenceService повертає Optional
                    redirectUrl = "/absences/" + targetId;
                    break;
                case "User":
                    exists = userService.findById(uuid) != null;
                    redirectUrl = "/users/" + targetId;
                    break;
                default:
                    exists = false;
            }
        } catch (Exception e) {
            // Якщо ID битий або інша помилка
            exists = false;
        }

        if (exists) {
            return "redirect:" + redirectUrl;
        } else {
            return "redirect:/events/" + logId + "/raw";
        }
    }

    @GetMapping("/{id}/raw")
    public String viewRawEvent(@PathVariable UUID id, Model model) {
        EventLog log = eventLogService.findById(id);
        if (log == null) return "redirect:/events";

        Map<String, Object> rawData = new LinkedHashMap<>();
        rawData.put("ID", log.getId());
        rawData.put("Timestamp", log.getTimestamp());
        rawData.put("Event", log.getEvent());
        rawData.put("Entity", log.getEntityName());
        rawData.put("Author", log.getAuthor());
        rawData.put("Target Name", log.getTargetName()); // Додали ім'я
        rawData.put("Target ID", log.getTargetId());

        if (log.getChangeDetails() != null) {
            rawData.put("--- CHANGES ---", "");
            rawData.putAll(log.getChangeDetails());
        }

        model.addAttribute("log", log);
        model.addAttribute("rawData", rawData);
        return "events/event-log-raw";
    }
}