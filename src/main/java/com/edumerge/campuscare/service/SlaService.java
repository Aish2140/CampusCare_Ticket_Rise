package com.edumerge.campuscare.service;

import com.edumerge.campuscare.entity.SlaPolicy;
import com.edumerge.campuscare.entity.TicketPriority;
import com.edumerge.campuscare.exception.ResourceNotFoundException;
import com.edumerge.campuscare.repository.SlaPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SlaService {

    private final SlaPolicyRepository slaPolicyRepository;

    public SlaService(SlaPolicyRepository slaPolicyRepository) {
        this.slaPolicyRepository = slaPolicyRepository;
    }

    public List<SlaPolicy> getAllPolicies() {
        return slaPolicyRepository.findAll();
    }

    public SlaPolicy getPolicyForPriority(TicketPriority priority) {
        return slaPolicyRepository.findByPriority(priority)
                .orElseGet(() -> {
                    // Fallback sensible defaults if policy not yet saved
                    return switch (priority) {
                        case URGENT -> new SlaPolicy(TicketPriority.URGENT, 30, 240, true);
                        case HIGH -> new SlaPolicy(TicketPriority.HIGH, 120, 1440, true);
                        case MEDIUM -> new SlaPolicy(TicketPriority.MEDIUM, 480, 2880, true);
                        case LOW -> new SlaPolicy(TicketPriority.LOW, 1440, 4320, true);
                    };
                });
    }

    public LocalDateTime calculateFirstResponseDueAt(TicketPriority priority, LocalDateTime fromTime) {
        SlaPolicy policy = getPolicyForPriority(priority);
        return fromTime.plusMinutes(policy.getFirstResponseMinutes());
    }

    public LocalDateTime calculateResolutionDueAt(TicketPriority priority, LocalDateTime fromTime) {
        SlaPolicy policy = getPolicyForPriority(priority);
        return fromTime.plusMinutes(policy.getResolutionMinutes());
    }

    @Transactional
    public SlaPolicy updatePolicy(Long id, int firstResponseMinutes, int resolutionMinutes, boolean pauseWhileWaiting) {
        SlaPolicy policy = slaPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA policy not found with id: " + id));
        policy.setFirstResponseMinutes(firstResponseMinutes);
        policy.setResolutionMinutes(resolutionMinutes);
        policy.setPauseWhileWaiting(pauseWhileWaiting);
        return slaPolicyRepository.save(policy);
    }
}
