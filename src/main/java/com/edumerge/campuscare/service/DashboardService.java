package com.edumerge.campuscare.service;

import com.edumerge.campuscare.dto.DashboardStatsDto;
import com.edumerge.campuscare.dto.StaffWorkloadDto;
import com.edumerge.campuscare.entity.*;
import com.edumerge.campuscare.repository.CategoryRepository;
import com.edumerge.campuscare.repository.TicketRepository;
import com.edumerge.campuscare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public DashboardService(TicketRepository ticketRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public DashboardStatsDto getAdminDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        LocalDateTime now = LocalDateTime.now();

        stats.setTotalTickets(ticketRepository.count());
        stats.setOpenTickets(ticketRepository.countByStatus(TicketStatus.OPEN));
        stats.setInProgressTickets(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS));
        stats.setWaitingTickets(ticketRepository.countByStatus(TicketStatus.WAITING_FOR_STUDENT));
        stats.setResolvedTickets(ticketRepository.countByStatus(TicketStatus.RESOLVED));
        stats.setClosedTickets(ticketRepository.countByStatus(TicketStatus.CLOSED));
        stats.setOverdueTickets(ticketRepository.countOverdueTickets(now));

        // Category distribution
        List<Object[]> catRows = ticketRepository.countTicketsByCategory();
        Map<String, Long> catDist = new LinkedHashMap<>();
        for (Object[] row : catRows) {
            catDist.put((String) row[0], (Long) row[1]);
        }
        stats.setCategoryDistribution(catDist);

        // Priority distribution
        List<Object[]> prioRows = ticketRepository.countTicketsByPriority();
        Map<String, Long> prioDist = new LinkedHashMap<>();
        for (Object[] row : prioRows) {
            TicketPriority prio = (TicketPriority) row[0];
            prioDist.put(prio.getDisplayName(), (Long) row[1]);
        }
        stats.setPriorityDistribution(prioDist);

        // Status distribution
        List<Object[]> statusRows = ticketRepository.countTicketsByStatus();
        Map<String, Long> statusDist = new LinkedHashMap<>();
        for (Object[] row : statusRows) {
            TicketStatus st = (TicketStatus) row[0];
            statusDist.put(st.getDisplayName(), (Long) row[1]);
        }
        stats.setStatusDistribution(statusDist);

        // All tickets for metrics calculation (ageing, resolution time, compliance)
        List<Ticket> allTickets = ticketRepository.findAll();
        long ageUnder24h = 0;
        long age1To3Days = 0;
        long age3To7Days = 0;
        long ageOver7Days = 0;

        long totalResolvedCount = 0;
        long compliantResolvedCount = 0;
        double totalResolutionMinutes = 0;

        for (Ticket t : allTickets) {
            // Ageing for pending (non-resolved, non-closed) tickets
            if (t.getStatus() != TicketStatus.RESOLVED && t.getStatus() != TicketStatus.CLOSED) {
                long hoursOld = Duration.between(t.getCreatedAt(), now).toHours();
                if (hoursOld < 24) {
                    ageUnder24h++;
                } else if (hoursOld < 72) {
                    age1To3Days++;
                } else if (hoursOld < 168) {
                    age3To7Days++;
                } else {
                    ageOver7Days++;
                }
            }

            // SLA compliance & Average resolution time for resolved tickets
            if (t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED) {
                if (t.getResolvedAt() != null) {
                    totalResolvedCount++;
                    double minutes = Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes();
                    totalResolutionMinutes += Math.max(0, minutes);
                    if (t.getResolutionDueAt() != null && !t.getResolvedAt().isAfter(t.getResolutionDueAt())) {
                        compliantResolvedCount++;
                    }
                }
            }
        }

        Map<String, Long> ageingMap = new LinkedHashMap<>();
        ageingMap.put("< 24 Hours", ageUnder24h);
        ageingMap.put("1–3 Days", age1To3Days);
        ageingMap.put("3–7 Days", age3To7Days);
        ageingMap.put("> 7 Days", ageOver7Days);
        stats.setAgeingBuckets(ageingMap);

        if (totalResolvedCount > 0) {
            double avgHours = (totalResolutionMinutes / totalResolvedCount) / 60.0;
            stats.setAverageResolutionHours(Math.round(avgHours * 10.0) / 10.0);
            double complianceRate = ((double) compliantResolvedCount / totalResolvedCount) * 100.0;
            stats.setSlaComplianceRate(Math.round(complianceRate * 10.0) / 10.0);
        } else {
            stats.setAverageResolutionHours(0.0);
            stats.setSlaComplianceRate(100.0);
        }

        // Staff workloads
        List<User> staffList = userRepository.findAllByRoleOrderByName(Role.ROLE_STAFF);
        List<StaffWorkloadDto> workloadDtos = new ArrayList<>();
        for (User staff : staffList) {
            long openCount = ticketRepository.countOpenTicketsByStaff(staff);
            long totalAssigned = ticketRepository.countByAssignedTo(staff);
            long overdueCount = ticketRepository.countOverdueTicketsForStaff(staff, now);
            String deptName = staff.getDepartment() != null ? staff.getDepartment().getName() : "Unassigned";

            workloadDtos.add(new StaffWorkloadDto(
                    staff.getId(),
                    staff.getFullName(),
                    staff.getEmail(),
                    deptName,
                    openCount,
                    totalAssigned,
                    overdueCount,
                    staff.isActive()
            ));
        }
        stats.setStaffWorkloads(workloadDtos);

        return stats;
    }

    public Map<String, Object> getStaffDashboardStats(User staff) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next4Hours = now.plusHours(4);

        long assignedTotal = ticketRepository.countByAssignedTo(staff);
        long assignedOpen = ticketRepository.countByAssignedToAndStatus(staff, TicketStatus.OPEN);
        long assignedInProgress = ticketRepository.countByAssignedToAndStatus(staff, TicketStatus.IN_PROGRESS);
        long assignedWaiting = ticketRepository.countByAssignedToAndStatus(staff, TicketStatus.WAITING_FOR_STUDENT);
        long assignedResolved = ticketRepository.countByAssignedToAndStatus(staff, TicketStatus.RESOLVED);
        long overdueCount = ticketRepository.countOverdueTicketsForStaff(staff, now);

        List<Ticket> approachingList = ticketRepository.findApproachingDeadlineTicketsForStaff(staff, now, next4Hours);
        List<Ticket> recentAssigned = ticketRepository.findByAssignedToOrderByCreatedAtDesc(staff);

        stats.put("assignedTotal", assignedTotal);
        stats.put("activeTickets", assignedOpen + assignedInProgress + assignedWaiting);
        stats.put("assignedResolved", assignedResolved);
        stats.put("overdueCount", overdueCount);
        stats.put("approachingCount", approachingList.size());
        stats.put("recentTickets", recentAssigned.stream().limit(6).toList());

        return stats;
    }

    public Map<String, Object> getStudentDashboardStats(User student) {
        Map<String, Object> stats = new HashMap<>();

        long total = ticketRepository.countByRequester(student);
        long open = ticketRepository.countByRequesterAndStatus(student, TicketStatus.OPEN);
        long inProgress = ticketRepository.countByRequesterAndStatus(student, TicketStatus.IN_PROGRESS);
        long waiting = ticketRepository.countByRequesterAndStatus(student, TicketStatus.WAITING_FOR_STUDENT);
        long resolved = ticketRepository.countByRequesterAndStatus(student, TicketStatus.RESOLVED);
        long closed = ticketRepository.countByRequesterAndStatus(student, TicketStatus.CLOSED);

        List<Ticket> recent = ticketRepository.findByRequesterOrderByCreatedAtDesc(student);

        stats.put("totalTickets", total);
        stats.put("activeTickets", open + inProgress + waiting);
        stats.put("waitingAction", waiting);
        stats.put("resolvedTickets", resolved + closed);
        stats.put("recentTickets", recent.stream().limit(5).toList());

        return stats;
    }
}
