package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.entity.Gender;
import com.msk.salaryflow.entity.Position;
import com.msk.salaryflow.service.EmployeeService;
import com.msk.salaryflow.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/employees")
@Controller
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    @GetMapping
    private String getEmployees(Model model,
                                @RequestParam(value = "q", required = false) String searchTerm,
                                @RequestParam(value = "deptId", required = false) UUID departmentId,
                                @RequestParam(value = "pos", required = false) Position position,
                                @RequestParam(value = "pensioners", required = false) Boolean pensioners,
                                @RequestParam(value = "minSalary", required = false) Double minSalary,
                                @RequestParam(value = "maxSalary", required = false) Double maxSalary,
                                @PageableDefault(sort = "lastName", direction = Sort.Direction.ASC, size = 10) Pageable pageable){

        if (minSalary != null && maxSalary != null && minSalary > maxSalary) {
            model.addAttribute("error", "Minimum salary cannot be greater than Maximum salary.");
            model.addAttribute("employees", Page.empty().getContent());
            model.addAttribute("page", Page.empty(pageable));
            model.addAttribute("minSalary", minSalary);
            model.addAttribute("maxSalary", maxSalary);
            model.addAttribute("currentSearch", searchTerm);
            model.addAttribute("currentDeptId", departmentId);
            model.addAttribute("currentPos", position);
            model.addAttribute("pensioners", pensioners);
            model.addAttribute("departmentList", departmentService.findAll(Pageable.unpaged()).getContent());
            model.addAttribute("positionList", Position.values());
            return "employees/employee-list";
        }

        Page<Employee> employees = employeeService.findAll(searchTerm, departmentId, position, pensioners, minSalary, maxSalary, pageable);

        model.addAttribute("employees", employees);
        model.addAttribute("currentSearch", searchTerm);
        model.addAttribute("currentDeptId", departmentId);
        model.addAttribute("currentPos", position);
        model.addAttribute("pensioners", pensioners);
        model.addAttribute("minSalary", minSalary);
        model.addAttribute("maxSalary", maxSalary);
        model.addAttribute("page", employees);
        model.addAttribute("departmentList", departmentService.findAll(Pageable.unpaged()).getContent());
        model.addAttribute("positionList", Position.values());

        return "employees/employee-list";
    }


    @PostMapping("/save")
    private String save(@Valid @ModelAttribute("employee") Employee employee,
                        BindingResult bindingResult,
                        @RequestParam(value = "birthdayDate", required = false) String birthdayDate,
                        @RequestParam(value = "hireDateStr", required = false) String hireDateStr,
                        Model model){

        if (employee.getPosition() == null) {
            employee.setPosition(Position.NONE);
        }
        Optional<Employee> existing = employeeService.findByEmail(employee.getEmail());
        if (existing.isPresent() && (employee.getId() == null || !existing.get().getId().equals(employee.getId()))) {
            bindingResult.rejectValue("email", "error.email", "This email is already in use.");
        }

        if (birthdayDate == null || birthdayDate.trim().isEmpty()) {
            bindingResult.rejectValue("birthday", "error.birthday", "Birthday is required");
        } else {
            try {
                LocalDate localDate = LocalDate.parse(birthdayDate);
                employee.setBirthday(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (Exception e) {
                bindingResult.rejectValue("birthday", "error.birthday", "Invalid birthday format");
            }
        }

        if (hireDateStr == null || hireDateStr.trim().isEmpty()) {
            bindingResult.rejectValue("hireDate", "error.hireDate", "Hire Date is required");
        } else {
            try {
                LocalDate localDate = LocalDate.parse(hireDateStr);
                employee.setHireDate(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (Exception e) {
                bindingResult.rejectValue("hireDate", "error.hireDate", "Invalid hire date format");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("genders", Gender.values());
            model.addAttribute("positions", Position.values());
            model.addAttribute("departments", departmentService.findAll(Pageable.unpaged()).getContent());
            return "employees/employee-form";
        }

        if(employee.getId()==null){
            employeeService.save(employee);
        } else{
            employeeService.update(employee);
        }
        return "redirect:/employees";
    }

    @GetMapping("/add")
    private String showFormForAdd(Model model){
        Employee employee = new Employee();
        model.addAttribute("employee", employee);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("positions", Position.values());
        model.addAttribute("departments", departmentService.findAll(Pageable.unpaged()).getContent());
        return "employees/employee-form";
    }

    @GetMapping("/update")
    private String showFormForUpdate(Model model, @RequestParam("employeeId") UUID id){
        Employee employee = employeeService.findById(id);
        if(employee == null) return "redirect:/employees";

        model.addAttribute("employee", employee);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("positions", Position.values());
        model.addAttribute("departments", departmentService.findAll(Pageable.unpaged()).getContent());
        return "employees/employee-form";
    }

    @GetMapping("/{id}")
    private String getEmployee(Model model, @PathVariable("id") UUID id){
        Employee employee = employeeService.findById(id);
        if(employee == null){
            return "redirect:/employees/notFound";
        }
        model.addAttribute("employee", employee);
        return "employees/employee-info";
    }

    @GetMapping("/delete")
    private String delete(@RequestParam("employeeId") UUID employeeId){
        employeeService.deleteById(employeeId);
        return "redirect:/employees";
    }
}