package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.dto.*;
import com.edumerge.campuscare.entity.*;
import com.edumerge.campuscare.service.CommentService;
import com.edumerge.campuscare.service.TicketService;
import com.edumerge.campuscare.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class ApiTicketController {

    private final TicketService ticketService;
    private final CommentService commentService;
    private final SecurityUtils securityUtils;

    public ApiTicketController(TicketService ticketService, CommentService commentService, SecurityUtils securityUtils) {
        this.ticketService = ticketService;
        this.commentService = commentService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Ticket>>> getTickets(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "department", required = false) Long departmentId,
            @RequestParam(value = "status", required = false) TicketStatus status,
            @RequestParam(value = "priority", required = false) TicketPriority priority,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        User currentUser = securityUtils.getCurrentUser();
        Page<Ticket> tickets = ticketService.filterTickets(currentUser, search, categoryId, departmentId, status, priority, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.ok(tickets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Ticket>> getTicket(@PathVariable("id") Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Ticket ticket = ticketService.getTicketById(id);
        ticketService.validateAccess(ticket, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(ticket));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Ticket>> createTicket(@Valid @RequestBody TicketCreateDto dto) {
        User currentUser = securityUtils.getCurrentUser();
        Ticket ticket = ticketService.createTicket(dto, currentUser, null, null);
        return ResponseEntity.ok(ApiResponse.ok("Ticket created successfully", ticket));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<Ticket>> assignTicket(@PathVariable("id") Long id, @Valid @RequestBody TicketAssignDto dto) {
        User actor = securityUtils.getCurrentUser();
        Ticket ticket = ticketService.assignTicket(id, dto.getStaffId(), actor);
        return ResponseEntity.ok(ApiResponse.ok("Ticket assigned successfully", ticket));
    }

    @GetMapping("/{id}/suggested-staff")
    public ResponseEntity<ApiResponse<User>> getSuggestedStaff(@PathVariable("id") Long id) {
        Optional<User> suggested = ticketService.suggestAssignee(id);
        return ResponseEntity.ok(ApiResponse.ok(suggested.orElse(null)));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Ticket>> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody TicketStatusUpdateDto dto) {
        User actor = securityUtils.getCurrentUser();
        Ticket ticket = ticketService.updateStatus(id, dto.getStatus(), dto.getNote(), actor);
        return ResponseEntity.ok(ApiResponse.ok("Status updated successfully", ticket));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<Ticket>> resolveTicket(@PathVariable("id") Long id, @Valid @RequestBody TicketResolveDto dto) {
        User actor = securityUtils.getCurrentUser();
        Ticket ticket = ticketService.resolveTicket(id, dto.getResolutionSummary(), actor);
        return ResponseEntity.ok(ApiResponse.ok("Ticket resolved successfully", ticket));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<Ticket>> closeTicket(@PathVariable("id") Long id) {
        User actor = securityUtils.getCurrentUser();
        Ticket ticket = ticketService.closeTicket(id, actor);
        return ResponseEntity.ok(ApiResponse.ok("Ticket closed successfully", ticket));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<Ticket>> reopenTicket(@PathVariable("id") Long id, @Valid @RequestBody TicketReopenDto dto) {
        User actor = securityUtils.getCurrentUser();
        Ticket ticket = ticketService.reopenTicket(id, dto.getReason(), actor);
        return ResponseEntity.ok(ApiResponse.ok("Ticket reopened successfully", ticket));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<TicketComment>>> getComments(@PathVariable("id") Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Ticket ticket = ticketService.getTicketById(id);
        ticketService.validateAccess(ticket, currentUser);
        List<TicketComment> comments = commentService.getCommentsForTicket(ticket, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(comments));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<TicketComment>> addComment(@PathVariable("id") Long id, @Valid @RequestBody CommentCreateDto dto) {
        User currentUser = securityUtils.getCurrentUser();
        TicketComment comment = commentService.addComment(id, dto.getContent(), dto.getVisibility(), currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Comment added successfully", comment));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<TicketHistory>>> getHistory(@PathVariable("id") Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Ticket ticket = ticketService.getTicketById(id);
        ticketService.validateAccess(ticket, currentUser);
        List<TicketHistory> history = ticketService.getTicketHistory(ticket);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}
