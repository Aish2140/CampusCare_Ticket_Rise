package com.edumerge.campuscare.config;

import com.edumerge.campuscare.entity.*;
import com.edumerge.campuscare.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final SlaPolicyRepository slaPolicyRepository;
    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(DepartmentRepository departmentRepository,
                      CategoryRepository categoryRepository,
                      UserRepository userRepository,
                      SlaPolicyRepository slaPolicyRepository,
                      TicketRepository ticketRepository,
                      TicketCommentRepository commentRepository,
                      TicketHistoryRepository historyRepository,
                      NotificationRepository notificationRepository,
                      PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return; // Seed data already exists
        }

        System.out.println(">>> Initializing CampusCare Seed Data...");

        // 1. SLA Policies (FR-030)
        SlaPolicy slaUrgent = slaPolicyRepository.save(new SlaPolicy(TicketPriority.URGENT, 30, 240, true)); // 30m response, 4h resolution
        SlaPolicy slaHigh = slaPolicyRepository.save(new SlaPolicy(TicketPriority.HIGH, 120, 1440, true));    // 2h response, 24h resolution
        SlaPolicy slaMedium = slaPolicyRepository.save(new SlaPolicy(TicketPriority.MEDIUM, 480, 2880, true)); // 8h response, 48h resolution
        SlaPolicy slaLow = slaPolicyRepository.save(new SlaPolicy(TicketPriority.LOW, 1440, 4320, true));    // 24h response, 72h resolution

        // 2. Departments (5.2)
        Department deptFinance = departmentRepository.save(new Department("Finance & Accounts", "Tuition fees, scholarships, payment receipts and refund inquiries"));
        Department deptAcademic = departmentRepository.save(new Department("Academic Affairs", "Transcripts, grade reports, syllabus, exam queries and certificates"));
        Department deptStudent = departmentRepository.save(new Department("Student Affairs", "ID cards, hostel accommodation, bus passes and campus grievances"));
        Department deptIT = departmentRepository.save(new Department("IT & Campus Services", "Wi-Fi access, student portal, lab computers and email accounts"));
        Department deptAdmin = departmentRepository.save(new Department("General Administration", "Admissions, verification letters, leaves and general queries"));

        // 3. Ticket Categories (5.4)
        Category catFee = categoryRepository.save(new Category("Fees and Payments", "Tuition installments, online payment receipts and fee clearance", deptFinance));
        Category catAttendance = categoryRepository.save(new Category("Attendance & Leaves", "Medical leaves, attendance correction and shortage appeals", deptAcademic));
        Category catIdCard = categoryRepository.save(new Category("ID Cards & Badges", "Lost ID card replacement, smart RFID card activation and barcode errors", deptStudent));
        Category catDocuments = categoryRepository.save(new Category("Documents & Certificates", "Bonafide letters, migration certificates and official transcripts", deptAcademic));
        Category catIT = categoryRepository.save(new Category("Technical Support & Lab Access", "Campus Wi-Fi connectivity, portal password reset and lab privileges", deptIT));
        Category catGeneral = categoryRepository.save(new Category("General Administration", "Hostel allotment, transport facilities and campus inquiries", deptAdmin));

        // 4. Users (Section 10 Seed Requirements)
        // 4.1 Admin (At least 1)
        User admin = new User("Dean Robert Miller", "admin@campuscare.edu", passwordEncoder.encode("Admin@123"), Role.ROLE_ADMIN, deptAdmin, "EMP-ADM-001");
        userRepository.save(admin);

        // 4.2 Support Staff (At least 4 across departments)
        User staffFinance = userRepository.save(new User("Priya Sharma", "staff.finance@campuscare.edu", passwordEncoder.encode("Staff@123"), Role.ROLE_STAFF, deptFinance, "EMP-FIN-101"));
        User staffAcademic = userRepository.save(new User("Rahul Verma", "staff.academics@campuscare.edu", passwordEncoder.encode("Staff@123"), Role.ROLE_STAFF, deptAcademic, "EMP-ACA-102"));
        User staffStudent = userRepository.save(new User("Ananya Desai", "staff.affairs@campuscare.edu", passwordEncoder.encode("Staff@123"), Role.ROLE_STAFF, deptStudent, "EMP-STU-103"));
        User staffIT = userRepository.save(new User("Karthik Nair", "staff.it@campuscare.edu", passwordEncoder.encode("Staff@123"), Role.ROLE_STAFF, deptIT, "EMP-ITS-104"));

        // 4.3 Students (At least 10)
        List<User> students = new ArrayList<>();
        String[] studentNames = {
            "Alex Johnson", "Sneha Patel", "Rohan Mehta", "Pooja Rao", "Vikram Singh",
            "Neha Kapoor", "Arjun Reddy", "Divya Krishnan", "Manish Joshi", "Tanvi Shah"
        };
        String[] studentEmails = {
            "student.alex@campuscare.edu", "student.sneha@campuscare.edu", "student.rohan@campuscare.edu",
            "student.pooja@campuscare.edu", "student.vikram@campuscare.edu", "student.neha@campuscare.edu",
            "student.arjun@campuscare.edu", "student.divya@campuscare.edu", "student.manish@campuscare.edu",
            "student.tanvi@campuscare.edu"
        };

        for (int i = 0; i < studentNames.length; i++) {
            User s = new User(studentNames[i], studentEmails[i], passwordEncoder.encode("Student@123"),
                    Role.ROLE_STUDENT, null, String.format("STU-2024-%03d", i + 1));
            students.add(userRepository.save(s));
        }

        // 5. Sample Tickets (At least 30 sample tickets across all statuses, priorities, ageing, comments, notes)
        LocalDateTime now = LocalDateTime.now();

        // Helper data structures
        record SeedTicketSpec(
            int daysAgo,
            User student,
            User assignee,
            Department dept,
            Category cat,
            TicketPriority priority,
            TicketStatus status,
            NextActionOwner nextAction,
            String subject,
            String description,
            String resolutionSummary,
            boolean isBreached,
            boolean hasFirstResponse
        ) {}

        List<SeedTicketSpec> specs = List.of(
            // --- Overdue Tickets (Ageing > 7 days or overdue resolution) ---
            new SeedTicketSpec(10, students.get(0), staffFinance, deptFinance, catFee, TicketPriority.HIGH, TicketStatus.IN_PROGRESS, NextActionOwner.STAFF,
                "Double deduction of semester fee during online gateway payment",
                "On September 22nd, I attempted to pay my 5th semester tuition fees through the HDFC gateway. The transaction failed on the screen but 45,000 INR was debited twice from my savings account.",
                null, true, true),

            new SeedTicketSpec(8, students.get(1), staffIT, deptIT, catIT, TicketPriority.URGENT, TicketStatus.OPEN, NextActionOwner.STAFF,
                "Server disconnection during final online programming lab exam",
                "My terminal disconnected 15 minutes before the end of the final exam session in Computer Lab 3. The supervisor told me to submit a ticket for session log recovery.",
                null, true, false),

            new SeedTicketSpec(9, students.get(2), staffAcademic, deptAcademic, catAttendance, TicketPriority.MEDIUM, TicketStatus.WAITING_FOR_STUDENT, NextActionOwner.STUDENT,
                "Attendance shortage waiver for state-level sports championship",
                "I was representing the college in the Inter-University Basketball Tournament from Sept 10 to Sept 18. Need 8 days of medical/duty leave credit added.",
                null, true, true),

            // --- Ageing 3-7 Days ---
            new SeedTicketSpec(5, students.get(3), staffStudent, deptStudent, catIdCard, TicketPriority.LOW, TicketStatus.IN_PROGRESS, NextActionOwner.STAFF,
                "RFID chip in student smart ID card not scanned at library turnstiles",
                "My card barcode is recognized by the librarian handheld scanner, but the automatic turnstiles blink red. Requesting RFID tag reprogramming.",
                null, false, true),

            new SeedTicketSpec(6, students.get(4), staffAcademic, deptAcademic, catDocuments, TicketPriority.HIGH, TicketStatus.RESOLVED, NextActionOwner.STUDENT,
                "Urgent official transcript required for German visa application",
                "I need a sealed transcript copy with Dean's signature by this Friday for my DAAD scholarship visa interview.",
                "Official transcripts (3 sealed copies) verified by Academic Registrar and issued at counter window 4 on Sept 28.", false, true),

            new SeedTicketSpec(4, students.get(5), staffFinance, deptFinance, catFee, TicketPriority.MEDIUM, TicketStatus.WAITING_FOR_STUDENT, NextActionOwner.STUDENT,
                "Merit scholarship fee waiver adjustment in 4th installment invoice",
                "I was awarded 25% merit scholarship for maintaining CGPA 9.2, but the installment bill shows full amount of 32,000 INR.",
                null, false, true),

            new SeedTicketSpec(5, students.get(6), null, deptIT, catIT, TicketPriority.HIGH, TicketStatus.OPEN, NextActionOwner.STAFF,
                "Cannot access college LMS Moodle portal from student Wi-Fi",
                "When connected to 'Campus-Student-5G', moodle.campuscare.edu gives ERR_CONNECTION_TIMED_OUT. Mobile data works fine.",
                null, true, false),

            new SeedTicketSpec(4, students.get(7), staffStudent, deptStudent, catGeneral, TicketPriority.LOW, TicketStatus.CLOSED, NextActionOwner.STAFF,
                "Hostel Block C hot water geyser malfunctioning on 3rd floor",
                "The solar water heater outlet on floor 3 has had zero pressure for 3 days.",
                "Maintenance plumbing technician replaced the valve and thermostat heating coil. Hot water supply restored.", false, true),

            // --- Ageing 1-3 Days ---
            new SeedTicketSpec(2, students.get(8), staffAcademic, deptAcademic, catDocuments, TicketPriority.URGENT, TicketStatus.IN_PROGRESS, NextActionOwner.STAFF,
                "Correction of mother's surname in Provisional Degree Certificate",
                "The provisional certificate displays 'Sunita' instead of 'Sunita Devi' matching my 10th CBSE marksheet. Attached 10th certificate copy.",
                null, false, true),

            new SeedTicketSpec(3, students.get(9), staffStudent, deptStudent, catIdCard, TicketPriority.MEDIUM, TicketStatus.RESOLVED, NextActionOwner.STUDENT,
                "Lost physical ID card during cultural fest at amphitheater",
                "Misplaced ID card between 4 PM and 7 PM on Saturday. Paid 200 INR replacement fee online.",
                "Replacement smart card printed and handed over to student with verification receipt.", false, true),

            new SeedTicketSpec(2, students.get(0), staffIT, deptIT, catIT, TicketPriority.MEDIUM, TicketStatus.OPEN, NextActionOwner.STAFF,
                "MATLAB license activation error on personal laptop",
                "Campus network license server returns error code -15: 'Cannot connect to license server system'.",
                null, false, false),

            new SeedTicketSpec(2, students.get(1), staffFinance, deptFinance, catFee, TicketPriority.LOW, TicketStatus.IN_PROGRESS, NextActionOwner.STAFF,
                "Request for fee installment payment schedule breakdown for educational loan",
                "Bank requires formal semester-wise fee disbursement breakdown signed by accounts officer.",
                null, false, true),

            new SeedTicketSpec(3, students.get(2), staffAcademic, deptAcademic, catAttendance, TicketPriority.HIGH, TicketStatus.CLOSED, NextActionOwner.STAFF,
                "Medical leave submission for dengue hospitalization (12 days)",
                "Hospitalized at City Care Hospital from Sept 5 to Sept 17. Doctor's fitness certificate attached.",
                "Medical board approved 12 days medical leave exemption. Attendance recalculated to 78.5%.", false, true),

            new SeedTicketSpec(1, students.get(3), null, deptGeneral(catGeneral), catGeneral, TicketPriority.LOW, TicketStatus.OPEN, NextActionOwner.STAFF,
                "College bus route 14 timing change request for morning shift",
                "Route 14 arrives at campus 25 minutes after first period starts due to newly opened flyover detour.",
                null, false, false),

            // --- Ageing < 24 Hours (Recent / Fresh) ---
            new SeedTicketSpec(0, students.get(4), staffIT, deptIT, catIT, TicketPriority.URGENT, TicketStatus.IN_PROGRESS, NextActionOwner.STAFF,
                "Campus email account locked out due to multiple failed passwords",
                "My institutional email (student.vikram@campuscare.edu) has been locked out. I have my mid-term project submission due today at 5 PM.",
                null, false, true),

            new SeedTicketSpec(0, students.get(5), null, deptFinance, catFee, TicketPriority.HIGH, TicketStatus.OPEN, NextActionOwner.STAFF,
                "Hostel mess deposit refund after vacating room 204",
                "I completed vacating formalities on Sept 30. Mess supervisor signed no-dues certificate. Please initiate refund of 5000 INR caution deposit.",
                null, false, false),

            new SeedTicketSpec(0, students.get(6), staffStudent, deptStudent, catIdCard, TicketPriority.LOW, TicketStatus.OPEN, NextActionOwner.STAFF,
                "Photo on portal profile is skewed and appears stretched",
                "Uploaded 3.5x4.5 cm passport photo during registration, but on the student dashboard it is distorted horizontally.",
                null, false, false),

            new SeedTicketSpec(0, students.get(7), staffAcademic, deptAcademic, catDocuments, TicketPriority.MEDIUM, TicketStatus.WAITING_FOR_STUDENT, NextActionOwner.STUDENT,
                "Application for Bonafide Certificate for National Passport renewal",
                "Need Bonafide Student Certificate stating current semester, academic year, and college address for passport seva kendra appointment.",
                null, false, true),

            new SeedTicketSpec(0, students.get(8), staffAcademic, deptAcademic, catAttendance, TicketPriority.LOW, TicketStatus.RESOLVED, NextActionOwner.STUDENT,
                "Attendance marked absent during Guest Lecture by ISRO scientist",
                "Attended the 2-hour guest lecture in Auditorium. Biometric machine failed to register punches.",
                "Event coordinator attendance sheet cross-referenced and 2 lecture credits granted.", false, true),

            new SeedTicketSpec(0, students.get(9), staffIT, deptIT, catIT, TicketPriority.MEDIUM, TicketStatus.OPEN, NextActionOwner.STAFF,
                "Unable to connect to Eduroam Wi-Fi network on Android 14",
                "The CA certificate dropdown asks for 'Use system certificates' and domain name. Please share proper config details.",
                null, false, false),

            // Additional tickets for depth (Total 32 tickets)
            new SeedTicketSpec(7, students.get(0), staffFinance, deptFinance, catFee, TicketPriority.LOW, TicketStatus.CLOSED, NextActionOwner.STAFF,
                "Receipt download link expired for 1st Year Bus Fee payment",
                "Need receipt for tax deduction under section 80C.",
                "Receipt duplicate generated and dispatched to registered email.", false, true),

            new SeedTicketSpec(11, students.get(1), staffAcademic, deptAcademic, catDocuments, TicketPriority.MEDIUM, TicketStatus.CLOSED, NextActionOwner.STAFF,
                "Recommendation letter request for CERN summer internship program",
                "Applying for CERN openlab summer student program. Need sealed institutional recommendation.",
                "Recommendation letter signed by Head of Department and issued to student.", false, true),

            new SeedTicketSpec(5, students.get(2), staffStudent, deptStudent, catGeneral, TicketPriority.LOW, TicketStatus.RESOLVED, NextActionOwner.STUDENT,
                "Gymnasium equipment repair request in Boys Sports Complex",
                "Two cable pulley machines are jammed and one bench press is unstable.",
                "Gym vendor inspected and replaced snapped steel cables and loose bolts.", false, true),

            new SeedTicketSpec(3, students.get(3), staffIT, deptIT, catIT, TicketPriority.HIGH, TicketStatus.WAITING_FOR_STUDENT, NextActionOwner.STUDENT,
                "Linux dual-boot installation corrupted Windows EFI partition in lab 4",
                "Laptop cannot boot into Windows after following lab OS partitioning instructions.",
                null, false, true),

            new SeedTicketSpec(1, students.get(4), null, deptAcademic, catAttendance, TicketPriority.MEDIUM, TicketStatus.OPEN, NextActionOwner.STAFF,
                "Clarification on electives attendance aggregation policy",
                "Does open elective attendance count towards the minimum 75% departmental criteria?",
                null, false, false),

            new SeedTicketSpec(2, students.get(5), staffStudent, deptStudent, catIdCard, TicketPriority.LOW, TicketStatus.IN_PROGRESS, NextActionOwner.STAFF,
                "Special access badge for Robotics club nocturnal hackathon",
                "Club members need permission to access Innovation Cell beyond 9 PM this weekend.",
                null, false, true),

            new SeedTicketSpec(6, students.get(6), staffFinance, deptFinance, catFee, TicketPriority.URGENT, TicketStatus.RESOLVED, NextActionOwner.STUDENT,
                "Late fee fine charged despite paying prior to deadline",
                "Fine of 1,500 INR applied to account even though NEFT was initiated 2 days before due date.",
                "NEFT UTR verified with bank reconciliation. Late fee waiver approved and credited to student wallet.", false, true),

            new SeedTicketSpec(4, students.get(7), staffIT, deptIT, catIT, TicketPriority.MEDIUM, TicketStatus.CLOSED, NextActionOwner.STAFF,
                "GitLab internal server commit pushes failing with HTTP 413",
                "Trying to push deep learning model weights file (~85MB) to internal git.campuscare.edu.",
                "Client body size limit on Nginx reverse proxy increased to 200MB. Pushes now successful.", false, true),

            new SeedTicketSpec(0, students.get(8), staffAcademic, deptAcademic, catDocuments, TicketPriority.URGENT, TicketStatus.OPEN, NextActionOwner.STAFF,
                "Medium of Instruction English certificate for Australian University application",
                "Deadlines close in 48 hours for Monash University graduate program.",
                null, false, false),

            new SeedTicketSpec(1, students.get(9), staffStudent, deptStudent, catGeneral, TicketPriority.LOW, TicketStatus.IN_PROGRESS, NextActionOwner.STAFF,
                "Parking sticker allocation for two-wheeler in South Gate parking lot",
                "Need vehicle registration sticker for Yamaha FZ registered in my name.",
                null, false, true),

            new SeedTicketSpec(8, students.get(0), staffAcademic, deptAcademic, catAttendance, TicketPriority.HIGH, TicketStatus.RESOLVED, NextActionOwner.STUDENT,
                "Duty leave approval for participating in Smart India Hackathon finals",
                "Participated in SIH 2026 Grand Finale at IIT Roorkee. Certificate and travel tickets verified.",
                "Duty leave for 5 working days granted with official participation certificate archived.", false, true),

            new SeedTicketSpec(2, students.get(1), null, deptFinance, catFee, TicketPriority.HIGH, TicketStatus.OPEN, NextActionOwner.STAFF,
                "Bank loan subsidy reimbursement credited to wrong registration ID",
                "Canara Bank education loan subsidy of 18,000 INR reflected against another student's account.",
                null, false, false)
        );

        int ticketCounter = 1;
        for (SeedTicketSpec s : specs) {
            Ticket ticket = new Ticket();
            ticket.setTicketNumber(String.format("CC-%d-%05d", now.getYear(), ticketCounter++));
            ticket.setRequester(s.student());
            ticket.setAssignedTo(s.assignee());
            ticket.setDepartment(s.dept());
            ticket.setCategory(s.cat());
            ticket.setSubject(s.subject());
            ticket.setDescription(s.description());
            ticket.setPriority(s.priority());
            ticket.setStatus(s.status());
            ticket.setNextActionOwner(s.nextAction());

            LocalDateTime createdTime = now.minusDays(s.daysAgo()).minusHours((ticketCounter * 3) % 20);
            ticket.setCreatedAt(createdTime);
            ticket.setUpdatedAt(createdTime.plusHours(1));

            // SLA Deadlines
            if (s.isBreached()) {
                // Past due dates
                ticket.setFirstResponseDueAt(createdTime.plusHours(2));
                ticket.setResolutionDueAt(createdTime.plusHours(8));
            } else {
                // Calculate from SLA policy
                SlaPolicy policy = slaPolicyRepository.findByPriority(s.priority()).orElse(slaMedium);
                ticket.setFirstResponseDueAt(createdTime.plusMinutes(policy.getFirstResponseMinutes()));
                ticket.setResolutionDueAt(createdTime.plusMinutes(policy.getResolutionMinutes()));
            }

            if (s.hasFirstResponse()) {
                ticket.setFirstRespondedAt(createdTime.plusMinutes(45));
            }

            if (s.status() == TicketStatus.RESOLVED || s.status() == TicketStatus.CLOSED) {
                ticket.setResolutionSummary(s.resolutionSummary());
                LocalDateTime resTime = createdTime.plusHours(s.isBreached() ? 30 : 6);
                ticket.setResolvedAt(resTime);
                if (s.status() == TicketStatus.CLOSED) {
                    ticket.setClosedAt(resTime.plusHours(24));
                }
            }

            Ticket savedTicket = ticketRepository.save(ticket);

            // Audit History
            historyRepository.save(new TicketHistory(savedTicket, s.student(), "CREATED", null, "OPEN",
                    "Ticket submitted by " + s.student().getFullName()));

            if (s.assignee() != null) {
                historyRepository.save(new TicketHistory(savedTicket, admin, "ASSIGNED", "Unassigned",
                        s.assignee().getFullName(), "Ticket assigned to " + s.assignee().getFullName()));
            }

            // Public & Internal Comments
            if (s.hasFirstResponse() && s.assignee() != null) {
                commentRepository.save(new TicketComment(savedTicket, s.assignee(),
                        "Hello " + s.student().getFullName() + ", thank you for reaching out. We have logged your request into our queue and are investigating the logs.",
                        CommentVisibility.PUBLIC));

                // Internal note for staff
                commentRepository.save(new TicketComment(savedTicket, s.assignee(),
                        "Internal review note: Verified student registration credentials against ERP database. Department head clearance initiated.",
                        CommentVisibility.INTERNAL));
            }

            if (s.status() == TicketStatus.WAITING_FOR_STUDENT && s.assignee() != null) {
                commentRepository.save(new TicketComment(savedTicket, s.assignee(),
                        "Please provide your transaction reference number and bank account statement screenshot so we can cross-reference the bank gateway records.",
                        CommentVisibility.PUBLIC));
            }

            if (s.status() == TicketStatus.RESOLVED || s.status() == TicketStatus.CLOSED) {
                commentRepository.save(new TicketComment(savedTicket, s.assignee() != null ? s.assignee() : admin,
                        "Resolution: " + s.resolutionSummary(),
                        CommentVisibility.PUBLIC));
            }

            // Notification
            if (s.status() == TicketStatus.RESOLVED) {
                notificationRepository.save(new Notification(s.student(), savedTicket, "RESOLVED",
                        "Your ticket " + savedTicket.getTicketNumber() + " has been resolved."));
            } else if (s.status() == TicketStatus.WAITING_FOR_STUDENT) {
                notificationRepository.save(new Notification(s.student(), savedTicket, "WAITING_FOR_STUDENT",
                        "Staff requested additional details on ticket " + savedTicket.getTicketNumber()));
            }
        }

        System.out.println(">>> CampusCare Seed Data initialized successfully!");
        System.out.println(">>> Users created: 1 Admin, 4 Staff, 10 Students");
        System.out.println(">>> Tickets created: " + specs.size() + " tickets");
    }

    private Department deptGeneral(Category cat) {
        return cat.getDepartment();
    }
}
