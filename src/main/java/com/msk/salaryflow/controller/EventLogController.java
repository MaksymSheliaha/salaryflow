package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.EventLog;
import com.msk.salaryflow.service.EventLogService;
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

    @GetMapping
    public String listEvents(@RequestParam(required = false) String from,
                             @RequestParam(required = false) String to,
                             @RequestParam(required = false) String eventType, // Новий фільтр
                             @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable, // Сортування за замовчуванням
                             Model model) {

        // Валідація дат
        if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate = LocalDate.parse(to);
            if (fromDate.isAfter(toDate)) {
                return "redirect:/events";
            }
        }

        // Передаємо eventType у сервіс
        Page<EventLog> eventLogs = eventLogService.getEventLogs(pageable, from, to, eventType);

        model.addAttribute("eventLogs", eventLogs.getContent());
        model.addAttribute("page", eventLogs);

        // Передаємо параметри назад у форму, щоб вони не зникали
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("eventType", eventType);

        // Для стрілочок сортування
        Sort.Order sortOrder = pageable.getSort().stream().findFirst().orElse(null);
        model.addAttribute("sortField", sortOrder != null ? sortOrder.getProperty() : "timestamp");
        model.addAttribute("sortDir", sortOrder != null ? sortOrder.getDirection().name().toLowerCase() : "desc");

        return "events/event-log-list";
    }

    // ... Інші методи (delete, open-target, raw) залишаються без змін ...

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
                                 @RequestParam(required = false) String to) {
        eventLogService.deleteByDateRange(from, to);
        return "redirect:/events";
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
        String redirectUrl;
        switch (entityName) {
            case "Employee" -> redirectUrl = "/employees/" + targetId;
            case "Department" -> redirectUrl = "/departments/" + targetId;
            case "Absence" -> redirectUrl = "/absences/" + targetId;
            default -> redirectUrl = "/";
        }
        return "redirect:" + redirectUrl;
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
        rawData.put("Target ID", log.getTargetId());

        model.addAttribute("log", log);
        model.addAttribute("rawData", rawData);
        return "events/event-log-raw";
    }
}