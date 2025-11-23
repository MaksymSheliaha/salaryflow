package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.Department;
import com.msk.salaryflow.entity.DepartmentInfo;
import com.msk.salaryflow.entity.Employee;
import com.msk.salaryflow.model.DepartmentSearchRequest;
// import com.msk.salaryflow.model.PageResponse; // Видаліть цей імпорт
import com.msk.salaryflow.service.DepartmentService;
import com.msk.salaryflow.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // Додайте цей імпорт
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/departments")
@Controller
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    @GetMapping
    private String getDepartments(Model model,
                                  @RequestParam(value = "q", required = false) String searchTerm,
                                  @RequestParam(value = "empInfo", defaultValue = "false") Boolean empInfo,
                                  @RequestParam(value = "employeeId", required = false) UUID employeeId,
                                  Pageable pageable){

        DepartmentSearchRequest request = new DepartmentSearchRequest(pageable, searchTerm, empInfo);

        // Змінюємо тип на Page
        Page<DepartmentInfo> page = departmentService.findAll(request);

        // Тепер .getContent() працює коректно, бо це стандартний Page
        model.addAttribute("departments", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("empInfoEnabled", Boolean.TRUE.equals(empInfo));
        return "departments/department-list";
    }

    // ... решта методів без змін ...
    @PostMapping("/save")
    private String save(@ModelAttribute("department") Department department){
        Department saved = departmentService.save(department);
        return "redirect:/departments/"+saved.getId();
    }
    // ... (весь інший код контролера залишається таким самим)
    @GetMapping("/add")
    private String showFormForAdd(Model model){
        Department department = new Department();
        model.addAttribute("department", department);
        return "departments/department-form";
    }

    @GetMapping("/update")
    private String showFormForUpdate(Model model, @ModelAttribute("departmentId")UUID id){
        Department department = departmentService.findById(id);
        if(department==null) return "redirect:/departments/notFound";
        model.addAttribute("department", department);
        return "departments/department-form";

    }

    @GetMapping("/{id}")
    private String getDepartment(Model model,
                                 @PathVariable("id") UUID id,
                                 @RequestParam(value = "empInfo", defaultValue = "false") Boolean empInfo){
        if (Boolean.TRUE.equals(empInfo)) {
            DepartmentInfo info = departmentService.findInfoById(id);
            if (info == null) {
                return "redirect:/departments";
            }
            model.addAttribute("department", info);
            model.addAttribute("empInfo", true);
        } else {
            Department department = departmentService.findById(id);
            if(department==null){
                return "redirect:/departments";
            }
            model.addAttribute("department", department);
            model.addAttribute("empInfo", false);
        }
        return "departments/department-info";
    }

    @PostMapping("/attach")
    private String attachEmployeeToDepartment(@RequestParam("departmentId") UUID departmentId,
                                              @RequestParam("employeeId") UUID employeeId) {
        Employee employee = employeeService.findById(employeeId);
        Department department = departmentService.findById(departmentId);
        if (employee != null && department != null) {
            employee.setDepartment(department);
            employeeService.save(employee);
        }
        return "redirect:/employees";
    }

    @GetMapping("/delete")
    private String delete(@ModelAttribute("departmentId") UUID departmentId){
        departmentService.deleteById(departmentId);
        return "redirect:/departments";
    }
}