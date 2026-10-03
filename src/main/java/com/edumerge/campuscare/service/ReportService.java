package com.edumerge.campuscare.service;

import com.edumerge.campuscare.entity.Ticket;
import com.edumerge.campuscare.entity.User;
import com.edumerge.campuscare.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    private final TicketRepository ticketRepository;

    public ReportService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Map<String, Object> generateReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Ticket> all = ticketRepository.findAll();
        List<Ticket> filtered = all.stream()
                .filter(t -> (startDate == null || !t.getCreatedAt().isBefore(startDate)))
                .filter(t -> (endDate == null || !t.getCreatedAt().isAfter(endDate)))
                .toList();

        Map<String, Object> report = new HashMap<>();
        report.put("totalVolume", filtered.size());

        Map<String, Long> categoryCounts = new HashMap<>();
        Map<String, Long> statusCounts = new HashMap<>();
        Map<String, Long> priorityCounts = new HashMap<>();

        long resolvedCount = 0;
        long onTimeCount = 0;
        double totalResolutionTimeMin = 0;

        for (Ticket t : filtered) {
            String cat = t.getCategory().getName();
            categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0L) + 1);

            String status = t.getStatus().getDisplayName();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);

            String prio = t.getPriority().getDisplayName();
            priorityCounts.put(prio, priorityCounts.getOrDefault(prio, 0L) + 1);

            if (t.getResolvedAt() != null) {
                resolvedCount++;
                double duration = Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes();
                totalResolutionTimeMin += Math.max(0, duration);
                if (t.getResolutionDueAt() != null && !t.getResolvedAt().isAfter(t.getResolutionDueAt())) {
                    onTimeCount++;
                }
            }
        }

        report.put("categoryBreakdown", categoryCounts);
        report.put("statusBreakdown", statusCounts);
        report.put("priorityBreakdown", priorityCounts);
        report.put("resolvedCount", resolvedCount);

        double avgHours = resolvedCount > 0 ? (totalResolutionTimeMin / resolvedCount) / 60.0 : 0.0;
        double slaRate = resolvedCount > 0 ? ((double) onTimeCount / resolvedCount) * 100.0 : 100.0;

        report.put("avgResolutionHours", Math.round(avgHours * 10.0) / 10.0);
        report.put("slaComplianceRate", Math.round(slaRate * 10.0) / 10.0);

        return report;
    }

    public void exportTicketsCsv(PrintWriter writer, User currentUser) {
        List<Ticket> tickets = ticketRepository.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        writer.println("Ticket Number,Subject,Requester,Requester Email,Department,Category,Priority,Status,Assignee,SLA Status,Created At,Resolved At");

        for (Ticket t : tickets) {
            String assignee = t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : "Unassigned";
            String resolved = t.getResolvedAt() != null ? t.getResolvedAt().format(fmt) : "N/A";
            String sla = t.getSlaStatus().getDisplayName();

            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    escapeCsv(t.getTicketNumber()),
                    escapeCsv(t.getSubject()),
                    escapeCsv(t.getRequester().getFullName()),
                    escapeCsv(t.getRequester().getEmail()),
                    escapeCsv(t.getDepartment().getName()),
                    escapeCsv(t.getCategory().getName()),
                    t.getPriority().name(),
                    t.getStatus().name(),
                    escapeCsv(assignee),
                    sla,
                    t.getCreatedAt().format(fmt),
                    resolved
            );
        }
        writer.flush();
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        return input.replace("\"", "\"\"");
    }
}
