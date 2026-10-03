package com.edumerge.campuscare.repository;

import com.edumerge.campuscare.entity.Ticket;
import com.edumerge.campuscare.entity.TicketPriority;
import com.edumerge.campuscare.entity.TicketStatus;
import com.edumerge.campuscare.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByRequesterOrderByCreatedAtDesc(User requester);

    List<Ticket> findByAssignedToOrderByCreatedAtDesc(User assignedTo);

    long countByStatus(TicketStatus status);

    long countByRequester(User requester);

    long countByRequesterAndStatus(User requester, TicketStatus status);

    long countByAssignedTo(User assignedTo);

    long countByAssignedToAndStatus(User assignedTo, TicketStatus status);

    long countByAssignedToAndStatusIn(User assignedTo, List<TicketStatus> statuses);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo = :assignedTo AND (t.status = 'OPEN' OR t.status = 'IN_PROGRESS' OR t.status = 'WAITING_FOR_STUDENT')")
    long countOpenTicketsByStaff(@Param("assignedTo") User assignedTo);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo IS NULL AND (t.status = 'OPEN' OR t.status = 'IN_PROGRESS')")
    long countUnassignedOpenTickets();

    @Query("SELECT t FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED') AND (t.resolutionDueAt < :now OR (t.firstRespondedAt IS NULL AND t.firstResponseDueAt < :now))")
    List<Ticket> findOverdueTickets(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED') AND (t.resolutionDueAt < :now OR (t.firstRespondedAt IS NULL AND t.firstResponseDueAt < :now))")
    long countOverdueTickets(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo = :staff AND t.status NOT IN ('RESOLVED', 'CLOSED') AND (t.resolutionDueAt < :now OR (t.firstRespondedAt IS NULL AND t.firstResponseDueAt < :now))")
    long countOverdueTicketsForStaff(@Param("staff") User staff, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Ticket t WHERE t.assignedTo = :staff AND t.status NOT IN ('RESOLVED', 'CLOSED') AND t.resolutionDueAt BETWEEN :now AND :upcoming")
    List<Ticket> findApproachingDeadlineTicketsForStaff(@Param("staff") User staff, @Param("now") LocalDateTime now, @Param("upcoming") LocalDateTime upcoming);

    @Query("SELECT c.name, COUNT(t) FROM Ticket t JOIN t.category c GROUP BY c.name")
    List<Object[]> countTicketsByCategory();

    @Query("SELECT t.priority, COUNT(t) FROM Ticket t GROUP BY t.priority")
    List<Object[]> countTicketsByPriority();

    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> countTicketsByStatus();

    @Query("SELECT t FROM Ticket t WHERE " +
           "(:requesterId IS NULL OR t.requester.id = :requesterId) AND " +
           "(:assignedToId IS NULL OR t.assignedTo.id = :assignedToId) AND " +
           "(:departmentId IS NULL OR t.department.id = :departmentId) AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:searchTerm IS NULL OR LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           " OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           " OR LOWER(t.requester.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Ticket> filterTickets(
            @Param("requesterId") Long requesterId,
            @Param("assignedToId") Long assignedToId,
            @Param("departmentId") Long departmentId,
            @Param("categoryId") Long categoryId,
            @Param("status") TicketStatus status,
            @Param("priority") TicketPriority priority,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}
