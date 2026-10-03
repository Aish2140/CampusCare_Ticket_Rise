package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.dto.TicketCreateDto;
import com.edumerge.campuscare.entity.*;
import com.edumerge.campuscare.service.*;
import com.edumerge.campuscare.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/tickets")
public class TicketWebController {

    private final TicketService ticketService;
    private final CommentService commentService;
    private final CategoryService categoryService;
    private final DepartmentService departmentService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    public TicketWebController(TicketService ticketService,
                               CommentService commentService,
                               CategoryService categoryService,
                               DepartmentService departmentService,
                               UserService userService,
                               SecurityUtils securityUtils) {
        this.ticketService = ticketService;
        this.commentService = commentService;
        this.categoryService = categoryService;
        this.departmentService = departmentService;
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String listTickets(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "department", required = false) Long departmentId,
            @RequestParam(value = "status", required = false) TicketStatus status,
            @RequestParam(value = "priority", required = false) TicketPriority priority,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            Model model) {

        User currentUser = securityUtils.getCurrentUser();
        Page<Ticket> ticketPage = ticketService.filterTickets(currentUser, search, categoryId, departmentId, status, priority, page, size, sortBy, sortDir);

        model.addAttribute("tickets", ticketPage.getContent());
        model.addAttribute("page", ticketPage);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("departments", departmentService.getActiveDepartments());
        model.addAttribute("allStatuses", TicketStatus.values());
        model.addAttribute("allPriorities", TicketPriority.values());

        model.addAttribute("currentSearch", search);
        model.addAttribute("currentCategory", categoryId);
        model.addAttribute("currentDepartment", departmentId);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentPriority", priority);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "tickets/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("ticketDto", new TicketCreateDto());
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("priorities", TicketPriority.values());
        return "tickets/create";
    }

    @PostMapping("/create")
    public String handleCreateTicket(
            @Valid @ModelAttribute("ticketDto") TicketCreateDto dto,
            BindingResult bindingResult,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", TicketPriority.values());
            return "tickets/create";
        }

        User currentUser = securityUtils.getCurrentUser();
        String attachmentName = null;
        String attachmentPath = null;

        if (file != null && !file.isEmpty()) {
            try {
                attachmentName = file.getOriginalFilename();
                String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();
                String storedFileName = UUID.randomUUID() + "_" + attachmentName;
                file.transferTo(new File(dir, storedFileName));
                attachmentPath = "/uploads/" + storedFileName;
            } catch (IOException e) {
                // proceed without failing ticket creation
            }
        }

        try {
            Ticket ticket = ticketService.createTicket(dto, currentUser, attachmentName, attachmentPath);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ticket " + ticket.getTicketNumber() + " submitted successfully!");
            return "redirect:/tickets/" + ticket.getId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", TicketPriority.values());
            return "tickets/create";
        }
    }

    @GetMapping("/{id}")
    public String viewTicket(@PathVariable("id") Long id, Model model) {
        User currentUser = securityUtils.getCurrentUser();
        Ticket ticket = ticketService.getTicketById(id);
        ticketService.validateAccess(ticket, currentUser);

        model.addAttribute("ticket", ticket);
        model.addAttribute("comments", commentService.getCommentsForTicket(ticket, currentUser));
        model.addAttribute("history", ticketService.getTicketHistory(ticket));
        model.addAttribute("allStatuses", TicketStatus.values());
        model.addAttribute("allPriorities", TicketPriority.values());

        // For Staff and Admins: Suggested staff recommendation & staff list
        if (currentUser.getRole() != Role.ROLE_STUDENT) {
            model.addAttribute("staffMembers", userService.getActiveStaff());
            Optional<User> suggested = ticketService.suggestAssignee(ticket.getId());
            model.addAttribute("suggestedStaff", suggested.orElse(null));
        }

        return "tickets/view";
    }

    @PostMapping("/{id}/assign")
    public String assignTicket(@PathVariable("id") Long id,
                               @RequestParam("staffId") Long staffId,
                               RedirectAttributes redirectAttributes) {
        User actor = securityUtils.getCurrentUser();
        try {
            ticketService.assignTicket(id, staffId, actor);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket successfully assigned.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") TicketStatus status,
                               @RequestParam(value = "note", required = false) String note,
                               RedirectAttributes redirectAttributes) {
        User actor = securityUtils.getCurrentUser();
        try {
            ticketService.updateStatus(id, status, note, actor);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket status updated to " + status.getDisplayName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/resolve")
    public String resolveTicket(@PathVariable("id") Long id,
                                @RequestParam("resolutionSummary") String resolutionSummary,
                                RedirectAttributes redirectAttributes) {
        User actor = securityUtils.getCurrentUser();
        try {
            ticketService.resolveTicket(id, resolutionSummary, actor);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket successfully resolved!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/close")
    public String closeTicket(@PathVariable("id") Long id,
                              RedirectAttributes redirectAttributes) {
        User actor = securityUtils.getCurrentUser();
        try {
            ticketService.closeTicket(id, actor);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket has been closed.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/reopen")
    public String reopenTicket(@PathVariable("id") Long id,
                               @RequestParam("reason") String reason,
                               RedirectAttributes redirectAttributes) {
        User actor = securityUtils.getCurrentUser();
        try {
            ticketService.reopenTicket(id, reason, actor);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket reopened and assigned to active queue.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/priority")
    public String changePriority(@PathVariable("id") Long id,
                                 @RequestParam("priority") TicketPriority priority,
                                 RedirectAttributes redirectAttributes) {
        User actor = securityUtils.getCurrentUser();
        try {
            ticketService.changePriority(id, priority, actor);
            redirectAttributes.addFlashAttribute("successMessage", "Priority updated and SLA deadline recalculated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable("id") Long id,
                             @RequestParam("content") String content,
                             @RequestParam(value = "visibility", defaultValue = "PUBLIC") CommentVisibility visibility,
                             RedirectAttributes redirectAttributes) {
        User actor = securityUtils.getCurrentUser();
        try {
            commentService.addComment(id, content, visibility, actor);
            redirectAttributes.addFlashAttribute("successMessage", "Comment posted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }
}
