package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.Department;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.entity.Gender;
import com.msk.salaryflow.entity.Position;
import com.msk.salaryflow.repository.DepartmentRepository;
import com.msk.salaryflow.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@RequestMapping("/employees")
@Controller
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @GetMapping
    private String getEmployees(Model model, Pageable pageable){
        Page<Employee> employees = employeeRepository.findAll(pageable);
        model.addAttribute("employees", employees.toList());
        return "employees/employee-list";
    }

    @PostMapping("/save")
    private String save(@ModelAttribute("employee") Employee employee,
                        @RequestParam(value = "birthdayDate", required = false) String birthdayDate){

        // Конвертируем birthday из String в Instant если нужно
        if (birthdayDate != null && !birthdayDate.isEmpty()) {
            LocalDate localDate = LocalDate.parse(birthdayDate);
            employee.setBirthday(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        if (employee.getHireDate() == null) {
            employee.setHireDate(Instant.now());
        }

        employeeRepository.save(employee);
        return "redirect:/employees";
    }

    @GetMapping("/add")
    private String showFormForAdd(Model model){
        Employee employee = new Employee();
        employee.setHireDate(Instant.now());
        employee.setSalary(0.0);

        model.addAttribute("employee", employee);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("positions", Position.values());
        model.addAttribute("departments", departmentRepository.findAll());
        return "employees/employee-form";
    }

    @GetMapping("/update")
    private String showFormForUpdate(Model model, @RequestParam("employeeId") UUID id){
        Employee employee = employeeRepository.findById(id).orElse(null);
        if(employee == null) return "redirect:/employees";

        model.addAttribute("employee", employee);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("positions", Position.values());
        model.addAttribute("departments", departmentRepository.findAll());
        return "employees/employee-form";
    }

    @GetMapping("/{id}")
    private String getEmployee(Model model, @PathVariable("id") UUID id){
        Employee employee = employeeRepository.findById(id).orElse(null);
        if(employee == null){
            return "redirect:/employees";
        }
        model.addAttribute("employee", employee);
        return "employees/employee-info";
    }

    @GetMapping("/delete")
    private String delete(@RequestParam("employeeId") UUID employeeId){
        employeeRepository.deleteById(employeeId);
        return "redirect:/employees";
    }
}