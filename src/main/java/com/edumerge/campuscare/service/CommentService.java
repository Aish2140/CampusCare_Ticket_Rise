package com.edumerge.campuscare.service;

import com.edumerge.campuscare.entity.*;
import com.edumerge.campuscare.exception.InvalidOperationException;
import com.edumerge.campuscare.exception.UnauthorizedException;
import com.edumerge.campuscare.repository.TicketCommentRepository;
import com.edumerge.campuscare.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final NotificationService notificationService;

    public CommentService(TicketCommentRepository ticketCommentRepository,
                          TicketRepository ticketRepository,
                          TicketService ticketService,
                          NotificationService notificationService) {
        this.ticketCommentRepository = ticketCommentRepository;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.notificationService = notificationService;
    }

    @Transactional
    public TicketComment addComment(Long ticketId, String content, CommentVisibility visibility, User author) {
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidOperationException("Comment content cannot be empty.");
        }

        Ticket ticket = ticketService.getTicketById(ticketId);
        ticketService.validateAccess(ticket, author);

        // Security check: Students can NEVER post or mark comments as INTERNAL notes (BR-006)
        if (author.getRole() == Role.ROLE_STUDENT && visibility == CommentVisibility.INTERNAL) {
            throw new UnauthorizedException("Students are not allowed to post internal notes.");
        }

        TicketComment comment = new TicketComment(ticket, author, content.trim(), visibility);
        TicketComment saved = ticketCommentRepository.save(comment);

        // Track First Response: If author is staff/admin and this is a public reply and ticket's first response is not recorded yet (FR-032)
        if (author.getRole() != Role.ROLE_STUDENT && visibility == CommentVisibility.PUBLIC) {
            if (ticket.getFirstRespondedAt() == null) {
                ticket.setFirstRespondedAt(LocalDateTime.now());
                ticketRepository.save(ticket);
            }
        }

        // FR-020: If student responds to a ticket in WAITING_FOR_STUDENT state, move ticket back to IN_PROGRESS
        if (author.getRole() == Role.ROLE_STUDENT && ticket.getStatus() == TicketStatus.WAITING_FOR_STUDENT) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            ticket.setNextActionOwner(NextActionOwner.STAFF);
            ticketRepository.save(ticket);
            ticketService.recordHistory(ticket, author, "STUDENT_RESPONSE",
                    TicketStatus.WAITING_FOR_STUDENT.name(), TicketStatus.IN_PROGRESS.name(),
                    "Student replied with additional details. Ticket returned to In Progress.");
        } else {
            String noteType = visibility == CommentVisibility.INTERNAL ? "Internal note" : "Public reply";
            ticketService.recordHistory(ticket, author, "COMMENT_ADDED", null, visibility.name(),
                    noteType + " added by " + author.getFullName());
        }

        // Notify relevant user
        if (visibility == CommentVisibility.PUBLIC) {
            if (author.getRole() == Role.ROLE_STUDENT) {
                if (ticket.getAssignedTo() != null) {
                    notificationService.sendNotification(ticket.getAssignedTo(), ticket, "STUDENT_REPLY",
                            "Student replied on ticket " + ticket.getTicketNumber());
                }
            } else {
                notificationService.sendNotification(ticket.getRequester(), ticket, "STAFF_REPLY",
                        "New staff update on ticket " + ticket.getTicketNumber());
            }
        }

        return saved;
    }

    public List<TicketComment> getCommentsForTicket(Ticket ticket, User currentUser) {
        if (currentUser.getRole() == Role.ROLE_STUDENT) {
            // Students can only see PUBLIC comments (BR-006, FR-026)
            return ticketCommentRepository.findByTicketAndVisibilityOrderByCreatedAtAsc(ticket, CommentVisibility.PUBLIC);
        } else {
            // Staff and Admins see both public comments and internal notes
            return ticketCommentRepository.findByTicketOrderByCreatedAtAsc(ticket);
        }
    }
}
