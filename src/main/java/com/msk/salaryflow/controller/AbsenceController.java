package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.entity.AbsenceType;
import com.msk.salaryflow.service.AbsenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/absences")
public class AbsenceController {

    private final AbsenceService service;

    @Autowired
    public AbsenceController(AbsenceService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<Absence> absences = service.findAll(PageRequest.of(page, size));
        model.addAttribute("absences", absences);
        return "absences/absence-list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        Absence absence = new Absence();
        absence.setStartDate(LocalDate.now());
        model.addAttribute("absence", absence);
        model.addAttribute("types", AbsenceType.values());
        return "absences/absence-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Absence absence,
                       BindingResult bindingResult,
                       Model model) {

        // перевірка, щоб endDate >= startDate
        if (absence.getStartDate() != null && absence.getEndDate() != null
                && absence.getStartDate().isAfter(absence.getEndDate())) {
            bindingResult.rejectValue("endDate", "endDate.invalid", "End date must be >= start date");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("types", AbsenceType.values());
            return "absences/absence-form";
        }

        service.save(absence);
        return "redirect:/absences";
    }

    @GetMapping("/update")
    public String updateForm(@RequestParam("id") UUID id, Model model) {
        Optional<Absence> opt = service.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/absences";
        }
        model.addAttribute("absence", opt.get());
        model.addAttribute("types", AbsenceType.values());
        return "absences/absence-form";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("id") UUID id) {
        service.deleteById(id);
        return "redirect:/absences";
    }

    @GetMapping("/{id}")
    public String info(@PathVariable UUID id, Model model) {
        Optional<Absence> opt = service.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/absences";
        }
        model.addAttribute("absence", opt.get());
        return "absences/absence-info";
    }
}

