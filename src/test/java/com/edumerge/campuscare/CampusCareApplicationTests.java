package com.edumerge.campuscare;

import com.edumerge.campuscare.dto.TicketCreateDto;
import com.edumerge.campuscare.entity.*;
import com.edumerge.campuscare.exception.InvalidOperationException;
import com.edumerge.campuscare.exception.UnauthorizedException;
import com.edumerge.campuscare.repository.CategoryRepository;
import com.edumerge.campuscare.repository.DepartmentRepository;
import com.edumerge.campuscare.repository.TicketRepository;
import com.edumerge.campuscare.repository.UserRepository;
import com.edumerge.campuscare.service.CommentService;
import com.edumerge.campuscare.service.DashboardService;
import com.edumerge.campuscare.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CampusCareApplicationTests {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    @DisplayName("T-004: Student creates a ticket - unique ID generated and SLA calculated")
    void testStudentCreateTicket() {
        User student = userRepository.findByEmailIgnoreCase("student.alex@campuscare.edu").orElseThrow();
        Category category = categoryRepository.findAll().get(0);

        TicketCreateDto dto = new TicketCreateDto();
        dto.setCategoryId(category.getId());
        dto.setSubject("Test Wi-Fi connectivity issue");
        dto.setDescription("Cannot connect to campus Wi-Fi in the main cafeteria area.");
        dto.setPriority(TicketPriority.HIGH);

        Ticket ticket = ticketService.createTicket(dto, student, null, null);

        assertNotNull(ticket.getId());
        assertNotNull(ticket.getTicketNumber());
        assertTrue(ticket.getTicketNumber().startsWith("CC-"));
        assertEquals(TicketStatus.OPEN, ticket.getStatus());
        assertEquals(NextActionOwner.STAFF, ticket.getNextActionOwner());
        assertNotNull(ticket.getFirstResponseDueAt());
        assertNotNull(ticket.getResolutionDueAt());
    }

    @Test
    @DisplayName("T-005: Student accesses another student's ticket - Access is denied")
    void testStudentAccessDeniedToOtherStudentTicket() {
        User student1 = userRepository.findByEmailIgnoreCase("student.alex@campuscare.edu").orElseThrow();
        User student2 = userRepository.findByEmailIgnoreCase("student.sneha@campuscare.edu").orElseThrow();
        Category category = categoryRepository.findAll().get(0);

        TicketCreateDto dto = new TicketCreateDto();
        dto.setCategoryId(category.getId());
        dto.setSubject("Private medical leave document");
        dto.setDescription("Submission of private medical certificates.");
        dto.setPriority(TicketPriority.MEDIUM);

        Ticket ticket = ticketService.createTicket(dto, student1, null, null);

        // Student 2 tries to access Student 1's ticket
        assertThrows(UnauthorizedException.class, () -> {
            ticketService.validateAccess(ticket, student2);
        });
    }

    @Test
    @DisplayName("T-006: Manager assigns a ticket - Assignee is updated and history is recorded")
    void testAssignTicketAndAuditHistory() {
        User admin = userRepository.findByEmailIgnoreCase("admin@campuscare.edu").orElseThrow();
        User staff = userRepository.findByEmailIgnoreCase("staff.finance@campuscare.edu").orElseThrow();
        User student = userRepository.findByEmailIgnoreCase("student.alex@campuscare.edu").orElseThrow();
        Category category = categoryRepository.findAll().get(0);

        TicketCreateDto dto = new TicketCreateDto();
        dto.setCategoryId(category.getId());
        dto.setSubject("Fee installment inquiry");
        dto.setDescription("Need breakdown of remaining fee installment.");
        dto.setPriority(TicketPriority.LOW);

        Ticket ticket = ticketService.createTicket(dto, student, null, null);

        Ticket assignedTicket = ticketService.assignTicket(ticket.getId(), staff.getId(), admin);
        assertEquals(staff.getId(), assignedTicket.getAssignedTo().getId());
        assertEquals(TicketStatus.IN_PROGRESS, assignedTicket.getStatus());

        List<TicketHistory> history = ticketService.getTicketHistory(assignedTicket);
        assertFalse(history.isEmpty());
        assertTrue(history.stream().anyMatch(h -> "ASSIGNED".equals(h.getActionType())));
    }

    @Test
    @DisplayName("T-008 & BR-006: Staff internal notes are hidden from students")
    void testInternalNotesHiddenFromStudent() {
        User admin = userRepository.findByEmailIgnoreCase("admin@campuscare.edu").orElseThrow();
        User staff = userRepository.findByEmailIgnoreCase("staff.it@campuscare.edu").orElseThrow();
        User student = userRepository.findByEmailIgnoreCase("student.alex@campuscare.edu").orElseThrow();
        Category category = categoryRepository.findAll().get(0);

        TicketCreateDto dto = new TicketCreateDto();
        dto.setCategoryId(category.getId());
        dto.setSubject("Technical account investigation");
        dto.setDescription("Account locked.");
        dto.setPriority(TicketPriority.HIGH);

        Ticket ticket = ticketService.createTicket(dto, student, null, null);

        // Staff adds a public reply
        commentService.addComment(ticket.getId(), "Hello, we are investigating your account.", CommentVisibility.PUBLIC, staff);

        // Staff adds an internal note
        commentService.addComment(ticket.getId(), "Confidential: Flagged for suspicious login attempts.", CommentVisibility.INTERNAL, staff);

        // Student views comments
        List<TicketComment> studentView = commentService.getCommentsForTicket(ticket, student);
        assertEquals(1, studentView.size());
        assertEquals(CommentVisibility.PUBLIC, studentView.get(0).getVisibility());

        // Staff views comments
        List<TicketComment> staffView = commentService.getCommentsForTicket(ticket, staff);
        assertEquals(2, staffView.size());
    }

    @Test
    @DisplayName("T-009 & BR-007: Staff resolves a ticket without a summary - Operation is rejected")
    void testResolveTicketWithoutSummaryFails() {
        User staff = userRepository.findByEmailIgnoreCase("staff.academic@campuscare.edu")
                .orElseGet(() -> userRepository.findAll().stream().filter(u -> u.getRole() == Role.ROLE_STAFF).findFirst().orElseThrow());
        User student = userRepository.findByEmailIgnoreCase("student.alex@campuscare.edu").orElseThrow();
        Category category = categoryRepository.findAll().get(0);

        TicketCreateDto dto = new TicketCreateDto();
        dto.setCategoryId(category.getId());
        dto.setSubject("Transcript request");
        dto.setDescription("Need transcript copy.");
        dto.setPriority(TicketPriority.MEDIUM);

        Ticket ticket = ticketService.createTicket(dto, student, null, null);

        assertThrows(InvalidOperationException.class, () -> {
            ticketService.resolveTicket(ticket.getId(), "", staff);
        });

        assertThrows(InvalidOperationException.class, () -> {
            ticketService.resolveTicket(ticket.getId(), "   ", staff);
        });
    }

    @Test
    @DisplayName("T-010: Staff resolves ticket with summary - Moves to Resolved")
    void testResolveTicketWithSummarySuccess() {
        User staff = userRepository.findAll().stream().filter(u -> u.getRole() == Role.ROLE_STAFF).findFirst().orElseThrow();
        User student = userRepository.findByEmailIgnoreCase("student.alex@campuscare.edu").orElseThrow();
        Category category = categoryRepository.findAll().get(0);

        TicketCreateDto dto = new TicketCreateDto();
        dto.setCategoryId(category.getId());
        dto.setSubject("Document request");
        dto.setDescription("Need bonafide certificate.");
        dto.setPriority(TicketPriority.LOW);

        Ticket ticket = ticketService.createTicket(dto, student, null, null);
        Ticket resolvedTicket = ticketService.resolveTicket(ticket.getId(), "Bonafide certificate generated and sealed.", staff);

        assertEquals(TicketStatus.RESOLVED, resolvedTicket.getStatus());
        assertNotNull(resolvedTicket.getResolvedAt());
        assertEquals("Bonafide certificate generated and sealed.", resolvedTicket.getResolutionSummary());
    }

    @Test
    @DisplayName("T-011: Student reopens an eligible resolved ticket - Returns to In Progress")
    void testStudentReopenTicket() {
        User staff = userRepository.findAll().stream().filter(u -> u.getRole() == Role.ROLE_STAFF).findFirst().orElseThrow();
        User student = userRepository.findByEmailIgnoreCase("student.alex@campuscare.edu").orElseThrow();
        Category category = categoryRepository.findAll().get(0);

        TicketCreateDto dto = new TicketCreateDto();
        dto.setCategoryId(category.getId());
        dto.setSubject("Hostel room maintenance");
        dto.setDescription("Fan not working in room 102.");
        dto.setPriority(TicketPriority.LOW);

        Ticket ticket = ticketService.createTicket(dto, student, null, null);
        ticketService.resolveTicket(ticket.getId(), "Technician checked the regulator.", staff);

        Ticket reopenedTicket = ticketService.reopenTicket(ticket.getId(), "Fan is still making loud grinding noise and vibrating.", student);

        assertEquals(TicketStatus.IN_PROGRESS, reopenedTicket.getStatus());
        assertEquals(1, reopenedTicket.getReopenCount());
        assertEquals(NextActionOwner.STAFF, reopenedTicket.getNextActionOwner());
    }

    @Test
    @DisplayName("T-015: Dashboard metrics are derived from persisted real data")
    void testDashboardMetricsDerivedFromPersistedData() {
        var stats = dashboardService.getAdminDashboardStats();
        assertTrue(stats.getTotalTickets() > 0);
        assertTrue(stats.getOpenTickets() >= 0);
        assertNotNull(stats.getCategoryDistribution());
        assertFalse(stats.getCategoryDistribution().isEmpty());
        assertNotNull(stats.getStaffWorkloads());
        assertFalse(stats.getStaffWorkloads().isEmpty());
    }
}
