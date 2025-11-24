package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.entity.Gender;
import com.msk.salaryflow.entity.Position;
import com.msk.salaryflow.service.EmployeeService;
import com.msk.salaryflow.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private String getEmployees(Model model,
                                @RequestParam(value = "q", required = false) String searchTerm,
                                @RequestParam(value = "deptId", required = false) UUID departmentId,
                                @RequestParam(value = "pos", required = false) Position position,
                                @PageableDefault(sort = "lastName", direction = Sort.Direction.ASC, size = 10) Pageable pageable){

        Page<Employee> employees = employeeService.findAll(searchTerm, departmentId, position, pageable);

        model.addAttribute("employees", employees);
        model.addAttribute("currentSearch", searchTerm);
        model.addAttribute("currentDeptId", departmentId);
        model.addAttribute("currentPos", position);
        model.addAttribute("page", employees);
        model.addAttribute("departmentList", departmentService.findAll(Pageable.unpaged()).getContent());
        model.addAttribute("positionList", Position.values());

        return "employees/employee-list";
    }

    @PostMapping("/save")
    private String save(@ModelAttribute("employee") Employee employee,
                        @RequestParam(value = "birthdayDate", required = false) String birthdayDate,
                        @RequestParam(value = "hireDateStr", required = false) String hireDateStr){ // Отримуємо рядок

        // Обробка Дня Народження
        if (birthdayDate != null && !birthdayDate.isEmpty()) {
            LocalDate localDate = LocalDate.parse(birthdayDate);
            employee.setBirthday(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        // Обробка Дати Найму (Hire Date)
        if (hireDateStr != null && !hireDateStr.isEmpty()) {
            LocalDate localDate = LocalDate.parse(hireDateStr);
            employee.setHireDate(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else if (employee.getHireDate() == null) {
            // Якщо пусто і це створення нового - ставимо сьогодні
            employee.setHireDate(Instant.now());
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
        // Встановлюємо дефолтну дату найму (сьогодні)
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