package com.msk.salaryflow.controller;

import com.msk.salaryflow.entity.Department;
import com.msk.salaryflow.entity.DepartmentInfo;
import com.msk.salaryflow.model.DepartmentSearchRequest;
import com.msk.salaryflow.model.PageResponse;
import com.msk.salaryflow.service.DepartmentService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    private String getDepartments(Model model,
                                  @RequestParam(value = "q", required = false) String searchTerm,
                                  @RequestParam(value = "empInfo", defaultValue = "false") Boolean empInfo,
                                  Pageable pageable){

        DepartmentSearchRequest request = new DepartmentSearchRequest(pageable, searchTerm, empInfo);
        PageResponse<DepartmentInfo> page = departmentService.findAll(request);
        model.addAttribute("departments", page.content());
        model.addAttribute("page", page);
        return "departments/department-list";
    }

    @PostMapping("/save")
    private String save(@ModelAttribute("department") Department department){
        Department saved = departmentService.save(department);
        return "redirect:/departments/"+saved.getId();
    }

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

    @GetMapping("/delete")
    private String delete(@ModelAttribute("departmentId") UUID departmentId){
        departmentService.deleteById(departmentId);
        return "redirect:/departments";
    }
}
