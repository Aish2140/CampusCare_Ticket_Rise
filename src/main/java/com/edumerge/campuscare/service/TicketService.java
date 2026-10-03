package com.edumerge.campuscare.service;

import com.edumerge.campuscare.dto.TicketCreateDto;
import com.edumerge.campuscare.entity.*;
import com.edumerge.campuscare.exception.InvalidOperationException;
import com.edumerge.campuscare.exception.ResourceNotFoundException;
import com.edumerge.campuscare.exception.UnauthorizedException;
import com.edumerge.campuscare.repository.TicketHistoryRepository;
import com.edumerge.campuscare.repository.TicketRepository;
import com.edumerge.campuscare.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final CategoryService categoryService;
    private final SlaService slaService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository,
                         TicketHistoryRepository ticketHistoryRepository,
                         CategoryService categoryService,
                         SlaService slaService,
                         NotificationService notificationService,
                         UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.categoryService = categoryService;
        this.slaService = slaService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    public synchronized String generateNextTicketNumber() {
        long count = ticketRepository.count() + 1;
        int year = LocalDateTime.now().getYear();
        return String.format("CC-%d-%05d", year, count);
    }

    @Transactional
    public Ticket createTicket(TicketCreateDto dto, User requester, String attachmentName, String attachmentPath) {
        if (requester.getRole() != Role.ROLE_STUDENT) {
            // Staff/Admins can also raise a request or students raise it
        }

        Category category = categoryService.getCategoryById(dto.getCategoryId());
        Department department = category.getDepartment();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstResponseDue = slaService.calculateFirstResponseDueAt(dto.getPriority(), now);
        LocalDateTime resolutionDue = slaService.calculateResolutionDueAt(dto.getPriority(), now);

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateNextTicketNumber());
        ticket.setRequester(requester);
        ticket.setDepartment(department);
        ticket.setCategory(category);
        ticket.setSubject(dto.getSubject());
        ticket.setDescription(dto.getDescription());
        ticket.setPriority(dto.getPriority());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setNextActionOwner(NextActionOwner.STAFF);
        ticket.setFirstResponseDueAt(firstResponseDue);
        ticket.setResolutionDueAt(resolutionDue);
        ticket.setAttachmentName(attachmentName);
        ticket.setAttachmentPath(attachmentPath);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        Ticket savedTicket = ticketRepository.save(ticket);

        // Record initial history
        recordHistory(savedTicket, requester, "CREATED", null, "OPEN", "Ticket created with subject: " + ticket.getSubject());

        // Notify admins / staff of new ticket
        List<User> activeStaff = userRepository.findByRoleAndDepartmentAndIsActiveTrue(Role.ROLE_STAFF, department);
        for (User staff : activeStaff) {
            notificationService.sendNotification(staff, savedTicket, "NEW_TICKET",
                    "New ticket " + savedTicket.getTicketNumber() + " submitted in " + category.getName());
        }

        return savedTicket;
    }

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    public Ticket getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketNumber));
    }

    public void validateAccess(Ticket ticket, User user) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            return;
        }
        if (user.getRole() == Role.ROLE_STAFF) {
            // Staff can access tickets assigned to them or within their department
            if (ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(user.getId())) {
                return;
            }
            if (user.getDepartment() != null && ticket.getDepartment().getId().equals(user.getDepartment().getId())) {
                return;
            }
            // Also allow staff to view tickets if authorized
            return;
        }
        if (user.getRole() == Role.ROLE_STUDENT) {
            if (!ticket.getRequester().getId().equals(user.getId())) {
                throw new UnauthorizedException("You are not authorized to view this ticket.");
            }
        }
    }

    @Transactional
    public Ticket assignTicket(Long ticketId, Long staffId, User actor) {
        if (actor.getRole() == Role.ROLE_STUDENT) {
            throw new UnauthorizedException("Students cannot assign tickets.");
        }

        Ticket ticket = getTicketById(ticketId);
        User newAssignee = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found with id: " + staffId));

        if (newAssignee.getRole() != Role.ROLE_STAFF && newAssignee.getRole() != Role.ROLE_ADMIN) {
            throw new InvalidOperationException("Can only assign tickets to active staff members.");
        }

        String oldAssigneeName = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFullName() : "Unassigned";
        ticket.setAssignedTo(newAssignee);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        ticket.setNextActionOwner(NextActionOwner.STAFF);
        Ticket saved = ticketRepository.save(ticket);

        recordHistory(saved, actor, "ASSIGNED", oldAssigneeName, newAssignee.getFullName(),
                "Assigned to " + newAssignee.getFullName() + " by " + actor.getFullName());

        notificationService.sendNotification(newAssignee, saved, "ASSIGNED",
                "Ticket " + saved.getTicketNumber() + " has been assigned to you.");

        return saved;
    }

    /**
     * Automatic assignment suggestion based on category specialization (department)
     * and current open-ticket workload (FR-016).
     */
    public Optional<User> suggestAssignee(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        Department dept = ticket.getDepartment();

        List<User> eligibleStaff = userRepository.findByRoleAndDepartmentAndIsActiveTrue(Role.ROLE_STAFF, dept);
        if (eligibleStaff.isEmpty()) {
            eligibleStaff = userRepository.findByRoleAndIsActiveTrue(Role.ROLE_STAFF);
        }

        if (eligibleStaff.isEmpty()) {
            return Optional.empty();
        }

        // Pick staff with the fewest open tickets
        return eligibleStaff.stream()
                .min(Comparator.comparingLong(ticketRepository::countOpenTicketsByStaff));
    }

    @Transactional
    public Ticket updateStatus(Long ticketId, TicketStatus newStatus, String note, User actor) {
        Ticket ticket = getTicketById(ticketId);
        validateAccess(ticket, actor);

        if (ticket.getStatus() == TicketStatus.CLOSED && actor.getRole() != Role.ROLE_ADMIN) {
            throw new InvalidOperationException("Closed tickets cannot be modified. Reopen if eligible.");
        }

        if (newStatus == TicketStatus.RESOLVED) {
            throw new InvalidOperationException("Use the resolve action and provide a resolution summary.");
        }

        TicketStatus oldStatus = ticket.getStatus();
        if (oldStatus == newStatus) {
            return ticket;
        }

        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.WAITING_FOR_STUDENT) {
            ticket.setNextActionOwner(NextActionOwner.STUDENT);
        } else if (newStatus == TicketStatus.IN_PROGRESS || newStatus == TicketStatus.OPEN) {
            ticket.setNextActionOwner(NextActionOwner.STAFF);
        }

        Ticket saved = ticketRepository.save(ticket);

        recordHistory(saved, actor, "STATUS_CHANGED", oldStatus.name(), newStatus.name(),
                note != null && !note.isBlank() ? note : "Status changed from " + oldStatus.getDisplayName() + " to " + newStatus.getDisplayName());

        notificationService.sendNotification(saved.getRequester(), saved, "STATUS_CHANGED",
                "Ticket " + saved.getTicketNumber() + " status updated to " + newStatus.getDisplayName());

        return saved;
    }

    @Transactional
    public Ticket requestInfoFromStudent(Long ticketId, String inquiry, User staff) {
        Ticket ticket = getTicketById(ticketId);
        if (staff.getRole() == Role.ROLE_STUDENT) {
            throw new UnauthorizedException("Only staff can request additional information.");
        }

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(TicketStatus.WAITING_FOR_STUDENT);
        ticket.setNextActionOwner(NextActionOwner.STUDENT);
        Ticket saved = ticketRepository.save(ticket);

        recordHistory(saved, staff, "WAITING_FOR_STUDENT", oldStatus.name(), TicketStatus.WAITING_FOR_STUDENT.name(),
                "Staff requested additional details: " + inquiry);

        notificationService.sendNotification(saved.getRequester(), saved, "INFO_REQUESTED",
                "Additional information requested for ticket " + saved.getTicketNumber() + ": " + inquiry);

        return saved;
    }

    @Transactional
    public Ticket resolveTicket(Long ticketId, String resolutionSummary, User actor) {
        if (actor.getRole() == Role.ROLE_STUDENT) {
            throw new UnauthorizedException("Students cannot resolve tickets.");
        }

        if (resolutionSummary == null || resolutionSummary.trim().length() < 5) {
            throw new InvalidOperationException("A clear resolution summary of at least 5 characters is required to resolve a ticket.");
        }

        Ticket ticket = getTicketById(ticketId);
        TicketStatus oldStatus = ticket.getStatus();

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolutionSummary(resolutionSummary.trim());
        ticket.setResolvedAt(LocalDateTime.now());
        ticket.setNextActionOwner(NextActionOwner.STUDENT);

        Ticket saved = ticketRepository.save(ticket);

        recordHistory(saved, actor, "RESOLVED", oldStatus.name(), TicketStatus.RESOLVED.name(),
                "Ticket resolved by " + actor.getFullName() + ". Summary: " + resolutionSummary.trim());

        notificationService.sendNotification(saved.getRequester(), saved, "RESOLVED",
                "Your ticket " + saved.getTicketNumber() + " has been marked as Resolved. You may review the resolution or reopen within 5 days.");

        return saved;
    }

    @Transactional
    public Ticket closeTicket(Long ticketId, User actor) {
        Ticket ticket = getTicketById(ticketId);
        TicketStatus oldStatus = ticket.getStatus();

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());
        Ticket saved = ticketRepository.save(ticket);

        recordHistory(saved, actor, "CLOSED", oldStatus.name(), TicketStatus.CLOSED.name(),
                "Ticket finalized and closed by " + actor.getFullName());

        notificationService.sendNotification(saved.getRequester(), saved, "CLOSED",
                "Ticket " + saved.getTicketNumber() + " is now closed.");

        return saved;
    }

    @Transactional
    public Ticket reopenTicket(Long ticketId, String reason, User student) {
        Ticket ticket = getTicketById(ticketId);

        if (student.getRole() == Role.ROLE_STUDENT && !ticket.getRequester().getId().equals(student.getId())) {
            throw new UnauthorizedException("You can only reopen your own tickets.");
        }

        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            throw new InvalidOperationException("Only resolved or closed tickets can be reopened.");
        }

        if (reason == null || reason.trim().length() < 5) {
            throw new InvalidOperationException("Please provide a valid reason (at least 5 characters) for reopening this ticket.");
        }

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setReopenCount(ticket.getReopenCount() + 1);
        ticket.setNextActionOwner(NextActionOwner.STAFF);

        Ticket saved = ticketRepository.save(ticket);

        recordHistory(saved, student, "REOPENED", oldStatus.name(), TicketStatus.IN_PROGRESS.name(),
                "Ticket reopened by " + student.getFullName() + ". Reason: " + reason.trim());

        if (saved.getAssignedTo() != null) {
            notificationService.sendNotification(saved.getAssignedTo(), saved, "REOPENED",
                    "Ticket " + saved.getTicketNumber() + " has been reopened: " + reason.trim());
        }

        return saved;
    }

    @Transactional
    public Ticket changePriority(Long ticketId, TicketPriority newPriority, User actor) {
        Ticket ticket = getTicketById(ticketId);
        if (actor.getRole() == Role.ROLE_STUDENT) {
            throw new UnauthorizedException("Students cannot change ticket priority after creation.");
        }

        TicketPriority oldPriority = ticket.getPriority();
        if (oldPriority == newPriority) {
            return ticket;
        }

        ticket.setPriority(newPriority);
        // Recalculate resolution deadline from creation time based on new priority policy (FR-035)
        LocalDateTime newResolutionDue = slaService.calculateResolutionDueAt(newPriority, ticket.getCreatedAt());
        ticket.setResolutionDueAt(newResolutionDue);

        Ticket saved = ticketRepository.save(ticket);

        recordHistory(saved, actor, "PRIORITY_CHANGED", oldPriority.name(), newPriority.name(),
                "Priority changed from " + oldPriority.getDisplayName() + " to " + newPriority.getDisplayName() + ". Deadline recalculated.");

        return saved;
    }

    public Page<Ticket> filterTickets(User currentUser,
                                      String searchTerm,
                                      Long categoryId,
                                      Long departmentId,
                                      TicketStatus status,
                                      TicketPriority priority,
                                      int page,
                                      int size,
                                      String sortBy,
                                      String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy != null && !sortBy.isBlank() ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(Math.max(0, page), size > 0 ? size : 10, sort);

        Long requesterId = null;
        Long assignedToId = null;

        if (currentUser.getRole() == Role.ROLE_STUDENT) {
            requesterId = currentUser.getId();
        } else if (currentUser.getRole() == Role.ROLE_STAFF) {
            // Staff can see assigned or department tickets, or specify in filter
            if (departmentId == null && currentUser.getDepartment() != null) {
                // Default to staff department if none specified
                departmentId = currentUser.getDepartment().getId();
            }
        }

        String search = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

        return ticketRepository.filterTickets(requesterId, assignedToId, departmentId, categoryId, status, priority, search, pageable);
    }

    public List<Ticket> getRecentTicketsForStudent(User student) {
        return ticketRepository.findByRequesterOrderByCreatedAtDesc(student);
    }

    public List<Ticket> getAssignedTicketsForStaff(User staff) {
        return ticketRepository.findByAssignedToOrderByCreatedAtDesc(staff);
    }

    @Transactional
    public void recordHistory(Ticket ticket, User actor, String actionType, String oldValue, String newValue, String description) {
        TicketHistory history = new TicketHistory(ticket, actor, actionType, oldValue, newValue, description);
        ticketHistoryRepository.save(history);
    }

    public List<TicketHistory> getTicketHistory(Ticket ticket) {
        return ticketHistoryRepository.findByTicketOrderByCreatedAtDesc(ticket);
    }
}
