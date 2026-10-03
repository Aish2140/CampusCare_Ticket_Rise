package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.service.CategoryService;
import com.edumerge.campuscare.service.DepartmentService;
import com.edumerge.campuscare.service.SlaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class SettingsWebController {

    private final CategoryService categoryService;
    private final DepartmentService departmentService;
    private final SlaService slaService;

    public SettingsWebController(CategoryService categoryService, DepartmentService departmentService, SlaService slaService) {
        this.categoryService = categoryService;
        this.departmentService = departmentService;
        this.slaService = slaService;
    }

    @GetMapping
    public String settingsPage(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("slaPolicies", slaService.getAllPolicies());
        return "admin/settings";
    }

    @PostMapping("/categories/create")
    public String createCategory(@RequestParam("name") String name,
                                 @RequestParam("description") String description,
                                 @RequestParam("departmentId") Long departmentId,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(name, description, departmentId);
            redirectAttributes.addFlashAttribute("successMessage", "Category added successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/sla/update")
    public String updateSla(@RequestParam("id") Long id,
                            @RequestParam("firstResponseMinutes") int firstResponseMinutes,
                            @RequestParam("resolutionMinutes") int resolutionMinutes,
                            @RequestParam(value = "pauseWhileWaiting", defaultValue = "false") boolean pauseWhileWaiting,
                            RedirectAttributes redirectAttributes) {
        try {
            slaService.updatePolicy(id, firstResponseMinutes, resolutionMinutes, pauseWhileWaiting);
            redirectAttributes.addFlashAttribute("successMessage", "SLA policy updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/departments/create")
    public String createDepartment(@RequestParam("name") String name,
                                   @RequestParam("description") String description,
                                   RedirectAttributes redirectAttributes) {
        try {
            departmentService.createDepartment(name, description);
            redirectAttributes.addFlashAttribute("successMessage", "Department created successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }
}
