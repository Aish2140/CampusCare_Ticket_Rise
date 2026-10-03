package com.edumerge.campuscare.repository;

import com.edumerge.campuscare.entity.SlaPolicy;
import com.edumerge.campuscare.entity.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    Optional<SlaPolicy> findByPriority(TicketPriority priority);
}
