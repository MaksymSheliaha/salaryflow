package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.EventLog;
import com.msk.salaryflow.service.EventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
                             Pageable pageable,
                             Model model) {
        Page<EventLog> eventLogs = eventLogService.getEventLogs(pageable, from, to);

        model.addAttribute("eventLogs", eventLogs.getContent());
        model.addAttribute("page", eventLogs);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "events/event-log-list";
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable UUID id,
                              @RequestParam(required = false) String from,
                              @RequestParam(required = false) String to,
                              Pageable pageable) {
        eventLogService.deleteById(id);

        return "redirect:/events?from=" + (from != null ? from : "") +
                "&to=" + (to != null ? to : "") +
                "&page=" + pageable.getPageNumber() +
                "&size=" + pageable.getPageSize();
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
        // Якщо дія видалення – показуємо raw-сторінку для самого лог-запису
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
        if (log == null) {
            return "redirect:/events";
        }

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
