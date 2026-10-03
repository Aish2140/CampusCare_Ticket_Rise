package com.edumerge.campuscare.repository;

import com.edumerge.campuscare.entity.Ticket;
import com.edumerge.campuscare.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
    List<TicketHistory> findByTicketOrderByCreatedAtDesc(Ticket ticket);
}
