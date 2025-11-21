package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.Department;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.entity.Gender;
import com.msk.salaryflow.entity.Position;
import com.msk.salaryflow.service.EmployeeService;
import com.msk.salaryflow.service.DepartmentService;
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
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    @GetMapping
    private String getEmployees(Model model, Pageable pageable){
        Page<Employee> employees = employeeService.findAll(pageable);
        model.addAttribute("employees", employees.toList());
        return "employees/employee-list";
    }

    @PostMapping("/save")
    private String save(@ModelAttribute("employee") Employee employee,
                        @RequestParam(value = "birthdayDate", required = false) String birthdayDate){

        if (birthdayDate != null && !birthdayDate.isEmpty()) {
            LocalDate localDate = LocalDate.parse(birthdayDate);
            employee.setBirthday(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        if (employee.getHireDate() == null) {
            employee.setHireDate(Instant.now());
        }

        employeeService.save(employee);
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