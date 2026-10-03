package com.edumerge.campuscare.repository;

import com.edumerge.campuscare.entity.CommentVisibility;
import com.edumerge.campuscare.entity.Ticket;
import com.edumerge.campuscare.entity.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketOrderByCreatedAtAsc(Ticket ticket);
    List<TicketComment> findByTicketAndVisibilityOrderByCreatedAtAsc(Ticket ticket, CommentVisibility visibility);
    long countByTicketAndVisibility(Ticket ticket, CommentVisibility visibility);
}
