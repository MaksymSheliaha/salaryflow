package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.entity.AbsenceType;
import com.msk.salaryflow.model.AbsenceResponse;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.service.AbsenceService;
import com.msk.salaryflow.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceService service;
    private final EmployeeService employeeService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       @RequestParam(value = "sort", defaultValue = "startDate,desc") String sort,
                       @RequestParam(value = "q", required = false) String searchTerm,
                       @RequestParam(value = "type", required = false) AbsenceType type) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Поле sortField тут не змінюємо, бо сортування відбувається в пам'яті в Service
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        Page<AbsenceResponse> absences = service.findAll(searchTerm, type, pageable);

        model.addAttribute("absences", absences);
        model.addAttribute("types", AbsenceType.values());
        model.addAttribute("currentType", type);
        model.addAttribute("currentSearch", searchTerm);
        return "absences/absence-list";
    }


    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Absence absence,
                       BindingResult bindingResult,
                       Model model) {
        if (absence.getStartDate() != null && absence.getEndDate() != null
                && absence.getStartDate().isAfter(absence.getEndDate())) {
            bindingResult.rejectValue("endDate", "endDate.invalid", "End date must be >= start date");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("types", AbsenceType.values());
            // Перевіряємо, чи є id працівника, і якщо є, намагаємося дістати його ім'я
            if (absence.getEmployeeId() != null) {
                try {
                    Employee employee = employeeService.findById(absence.getEmployeeId());
                    if (employee != null) {
                        model.addAttribute("selectedEmployeeName", employee.getFirstName() + " " + employee.getLastName());
                    }
                } catch (Exception ignored) {}
            }
            // Визначаємо, чи це новий запис, для коректної поведінки форми
            model.addAttribute("isNew", absence.getId() == null);
            return "absences/absence-form";
        }
        if(absence.getId()==null){
            service.save(absence);
        } else{
            service.update(absence);
        }
        return "redirect:/absences";
    }


    @GetMapping("/delete")
    public String delete(@RequestParam("id") UUID id) {
        service.deleteById(id);
        return "redirect:/absences";
    }

    @GetMapping("/{id}")
    public String info(@PathVariable UUID id, Model model) {
        AbsenceResponse response = service.findByIdResponse(id);
        if (response == null) {
            return "redirect:/absences";
        }
        model.addAttribute("absence", response);
        return "absences/absence-info";
    }
    @GetMapping("/add")
    public String addForm(Model model,
                          @RequestParam(value = "employeeId", required = false) UUID employeeId) {

        Absence absence = new Absence();
        absence.setStartDate(LocalDate.now());

        if (employeeId != null) {
            absence.setEmployeeId(employeeId); // employeeId вже UUID
            Employee employee = employeeService.findById(employeeId);
            if (employee != null) {
                model.addAttribute("selectedEmployeeName", employee.getFirstName() + " " + employee.getLastName());
            }
        }

        model.addAttribute("absence", absence);
        model.addAttribute("types", AbsenceType.values());
        model.addAttribute("isNew", true);

        return "absences/absence-form";
    }

    @GetMapping("/update")
    public String updateForm(@RequestParam("id") UUID id, Model model) {
        Optional<Absence> opt = service.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/absences";
        }
        Absence absence = opt.get();
        model.addAttribute("absence", absence);
        model.addAttribute("types", AbsenceType.values());

        if (absence.getEmployeeId() != null) {
            // employeeId вже UUID, не потрібно парсити
            Employee employee = employeeService.findById(absence.getEmployeeId());
            if (employee != null) {
                model.addAttribute("selectedEmployeeName", employee.getFirstName() + " " + employee.getLastName());
            }
        }

        model.addAttribute("isNew", false);

        return "absences/absence-form";
    }
}