
package com.hostelcare.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.hostelcare.dto.ApiDtos.ComplaintRequest;
import com.hostelcare.dto.ApiDtos.FeedbackRequest;
import com.hostelcare.dto.ApiDtos.StatusRequest;
import com.hostelcare.entity.Assignment;
import com.hostelcare.entity.Complaint;
import com.hostelcare.entity.ComplaintAttachment;
import com.hostelcare.entity.ComplaintHistory;
import com.hostelcare.entity.Feedback;
import com.hostelcare.entity.User;
import com.hostelcare.enums.ComplaintStatus;
import com.hostelcare.enums.Priority;
import com.hostelcare.enums.Role;
import com.hostelcare.repository.AssignmentRepository;
import com.hostelcare.repository.CategoryRepository;
import com.hostelcare.repository.ComplaintAttachmentRepository;
import com.hostelcare.repository.ComplaintHistoryRepository;
import com.hostelcare.repository.ComplaintRepository;
import com.hostelcare.repository.FeedbackRepository;
import com.hostelcare.repository.UserRepository;

@Service
public class ComplaintService {

    private final ComplaintRepository complaints;
    private final UserRepository users;
    private final CategoryRepository categories;
    private final ComplaintHistoryRepository histories;
    private final AssignmentRepository assignments;
    private final ComplaintAttachmentRepository attachments;
    private final FeedbackRepository feedbacks;
    private final EmailService email;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public ComplaintService(
            ComplaintRepository complaints,
            UserRepository users,
            CategoryRepository categories,
            ComplaintHistoryRepository histories,
            AssignmentRepository assignments,
            ComplaintAttachmentRepository attachments,
            FeedbackRepository feedbacks,
            EmailService email) {

        this.complaints = complaints;
        this.users = users;
        this.categories = categories;
        this.histories = histories;
        this.assignments = assignments;
        this.attachments = attachments;
        this.feedbacks = feedbacks;
        this.email = email;
    }

    // 1. Create a complaint
    @Transactional
    public Complaint create(
            String emailAddress,
            ComplaintRequest req,
            List<MultipartFile> files) {

        User student = user(emailAddress);

        if (student.role != Role.STUDENT) {
            throw new IllegalArgumentException(
                    "Only students can register complaints.");
        }

        if (student.room == null) {
            throw new IllegalArgumentException(
                    "Your profile has no room assigned. Contact the warden.");
        }

        if (files != null && files.size() > 3) {
            throw new IllegalArgumentException(
                    "Maximum 3 images are allowed.");
        }

        Complaint complaint = new Complaint();

        complaint.student = student;
        complaint.room = student.room;

        complaint.category = categories.findById(req.categoryId())
                .orElseThrow(() ->
                        new NoSuchElementException("Category not found"));

        complaint.title = req.title().trim();
        complaint.description = req.description().trim();

        complaints.save(complaint);

        history(
                complaint,
                student,
                null,
                complaint.status.name(),
                "Complaint submitted"
        );

        if (files != null) {
            for (MultipartFile file : files) {
                saveFile(complaint, file);
            }
        }

        afterCommit(() ->
                email.notify(
                        student.email,
                        "HostelCare complaint registered",
                        "Your complaint #" + complaint.complaintId
                                + " has been submitted."
                )
        );

        return complaint;
    }

    // 2. Save complaint image
    private void saveFile(Complaint complaint, MultipartFile file) {

        try {

            if (file == null || file.isEmpty()) {
                return;
            }

            if (file.getSize() > 5L * 1024 * 1024) {
                throw new IllegalArgumentException(
                        "Each image must be 5 MB or smaller.");
            }

            String type = Optional.ofNullable(file.getContentType())
                    .orElse("");

            if (!List.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            ).contains(type)) {

                throw new IllegalArgumentException(
                        "Only JPEG, PNG and WebP images are allowed.");
            }

            if (!validImageSignature(file, type)) {
                throw new IllegalArgumentException(
                        "Invalid image file.");
            }

            String extension;

            if (type.equals("image/png")) {
                extension = ".png";
            } else if (type.equals("image/webp")) {
                extension = ".webp";
            } else {
                extension = ".jpg";
            }

            String fileName = UUID.randomUUID() + extension;

            Path directory = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(directory);

            Path destination = directory.resolve(fileName).normalize();

            if (!destination.startsWith(directory)) {
                throw new IllegalArgumentException(
                        "Invalid file destination.");
            }

            file.transferTo(destination);

            ComplaintAttachment attachment = new ComplaintAttachment();

            attachment.complaint = complaint;
            attachment.fileName = fileName;
            attachment.filePath = destination.toString();

            attachments.save(attachment);

        } catch (IOException ex) {

            throw new IllegalArgumentException(
                    "Could not store uploaded image.", ex);
        }
    }

    // 3. Get student's complaints
    public List<Complaint> mine(String emailAddress) {

        User student = user(emailAddress);

        return complaints.findByStudentUserIdOrderByCreatedAtDesc(
                student.userId
        );
    }

    // 4. Get complaint with access verification
    public Complaint get(Long id, String emailAddress) {

        Complaint complaint = complaints.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Complaint not found"));

        User currentUser = user(emailAddress);

        boolean assigned = assignments
                .findByComplaintComplaintIdAndActiveTrue(id)
                .stream()
                .anyMatch(a ->
                        Objects.equals(
                                a.staff.userId,
                                currentUser.userId
                        )
                );

        if (currentUser.role == Role.ADMIN
                || Objects.equals(
                        complaint.student.userId,
                        currentUser.userId)
                || (currentUser.role == Role.STAFF && assigned)) {

            return complaint;
        }

        throw new NoSuchElementException("Complaint not found");
    }

    // 5. Update complaint status
    @Transactional
    public Complaint updateStatus(
            Long id,
            String emailAddress,
            StatusRequest req,
            boolean staffAction) {

        Complaint complaint = complaints.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Complaint not found"));

        User actor = user(emailAddress);

        ComplaintStatus nextStatus;

        try {
            nextStatus = ComplaintStatus.valueOf(
                    req.status().trim().toUpperCase()
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Invalid complaint status.");
        }

        if (staffAction) {

            boolean assigned = assignments
                    .findByComplaintComplaintIdAndActiveTrue(id)
                    .stream()
                    .anyMatch(a ->
                            Objects.equals(
                                    a.staff.userId,
                                    actor.userId
                            )
                    );

            if (actor.role != Role.ADMIN
                    && (actor.role != Role.STAFF || !assigned)) {

                throw new NoSuchElementException("Complaint not found");
            }

            if (!List.of(
                    ComplaintStatus.IN_PROGRESS,
                    ComplaintStatus.RESOLVED
            ).contains(nextStatus)) {

                throw new IllegalArgumentException(
                        "Staff can set status to IN_PROGRESS or RESOLVED.");
            }

            if (!List.of(
                    ComplaintStatus.ASSIGNED,
                    ComplaintStatus.IN_PROGRESS,
                    ComplaintStatus.REOPENED
            ).contains(complaint.status)) {

                throw new IllegalArgumentException(
                        "Complaint cannot be updated from its current status.");
            }

        } else if (actor.role == Role.STUDENT) {

            if (!Objects.equals(
                    complaint.student.userId,
                    actor.userId)) {

                throw new NoSuchElementException("Complaint not found");
            }

            if (nextStatus != ComplaintStatus.CLOSED
                    && nextStatus != ComplaintStatus.REOPENED) {

                throw new IllegalArgumentException(
                        "Students can only close or reopen resolved complaints.");
            }

            if (complaint.status != ComplaintStatus.RESOLVED) {

                throw new IllegalArgumentException(
                        "Only resolved complaints can be closed or reopened.");
            }

        } else if (actor.role != Role.ADMIN) {

            throw new IllegalArgumentException("Not authorized.");
        }

        if (complaint.status == nextStatus) {
            throw new IllegalArgumentException(
                    "Complaint is already in this status.");
        }

        String oldStatus = complaint.status.name();

        complaint.status = nextStatus;
        complaint.updatedAt = LocalDateTime.now();

        complaints.save(complaint);

        history(
                complaint,
                actor,
                oldStatus,
                nextStatus.name(),
                req.remarks()
        );

        afterCommit(() ->
                email.notify(
                        complaint.student.email,
                        "HostelCare complaint update",
                        "Complaint #" + id
                                + " status: " + nextStatus
                )
        );

        return complaint;
    }

    // 6. Set complaint priority
    @Transactional
    public void setPriority(Long id, String priorityValue) {

        Complaint complaint = complaints.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Complaint not found"));

        try {
            complaint.priority = Priority.valueOf(
                    priorityValue.trim().toUpperCase()
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Priority must be LOW, MEDIUM or HIGH.");
        }

        complaint.updatedAt = LocalDateTime.now();

        complaints.save(complaint);
    }

    // 7. Assign complaint to staff
    @Transactional
    public void assign(
            Long id,
            Long staffId,
            String adminEmail) {

        Complaint complaint = complaints.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Complaint not found"));

        User staff = users.findById(staffId)
                .orElseThrow(() ->
                        new NoSuchElementException("Staff not found"));

        if (staff.role != Role.STAFF || !staff.active) {
            throw new IllegalArgumentException(
                    "Select an active staff account.");
        }

        User admin = user(adminEmail);

        if (admin.role != Role.ADMIN) {
            throw new IllegalArgumentException(
                    "Only administrators can assign complaints.");
        }

        assignments
                .findByComplaintComplaintIdAndActiveTrue(id)
                .forEach(assignment -> {
                    assignment.active = false;
                    assignments.save(assignment);
                });

        Assignment assignment = new Assignment();

        assignment.complaint = complaint;
        assignment.staff = staff;
        assignment.assignedBy = admin;

        assignments.save(assignment);

        String oldStatus = complaint.status.name();

        complaint.status = ComplaintStatus.ASSIGNED;
        complaint.updatedAt = LocalDateTime.now();

        complaints.save(complaint);

        history(
                complaint,
                admin,
                oldStatus,
                ComplaintStatus.ASSIGNED.name(),
                "Assigned to " + staff.name
        );

        afterCommit(() -> {

            email.notify(
                    complaint.student.email,
                    "HostelCare complaint assigned",
                    "Complaint #" + id + " assigned to " + staff.name
            );

            email.notify(
                    staff.email,
                    "New HostelCare task",
                    "Complaint #" + id + " has been assigned to you."
            );
        });
    }

    // 8. Submit student feedback
    @Transactional
    public void feedback(
            Long id,
            String emailAddress,
            FeedbackRequest req) {

        User student = user(emailAddress);

        Complaint complaint = complaints.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Complaint not found"));

        if (!Objects.equals(
                complaint.student.userId,
                student.userId)) {

            throw new NoSuchElementException("Complaint not found");
        }

        if (complaint.status != ComplaintStatus.RESOLVED
                && complaint.status != ComplaintStatus.CLOSED) {

            throw new IllegalArgumentException(
                    "Feedback is available after resolution.");
        }

        if (feedbacks.existsByComplaintComplaintId(id)) {
            throw new IllegalArgumentException(
                    "Feedback has already been submitted.");
        }

        if (req.rating() == null
                || req.rating() < 1
                || req.rating() > 5) {

            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5.");
        }

        Feedback feedback = new Feedback();

        feedback.complaint = complaint;
        feedback.student = student;
        feedback.rating = req.rating();
        feedback.comments = req.comments();

        feedbacks.save(feedback);
    }

    // 9. Execute email after successful transaction
    private void afterCommit(Runnable action) {

        if (TransactionSynchronizationManager
                .isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {
                            action.run();
                        }
                    }
            );

        } else {
            action.run();
        }
    }

    // 10. Validate image signatures
    private boolean validImageSignature(
            MultipartFile file,
            String type) throws IOException {

        byte[] bytes = file.getBytes();

        if (type.equals("image/png")) {

            return bytes.length >= 8
                    && (bytes[0] & 255) == 137
                    && bytes[1] == 80
                    && bytes[2] == 78
                    && bytes[3] == 71
                    && bytes[4] == 13
                    && bytes[5] == 10
                    && bytes[6] == 26
                    && bytes[7] == 10;
        }

        if (type.equals("image/jpeg")) {

            return bytes.length >= 3
                    && (bytes[0] & 255) == 255
                    && (bytes[1] & 255) == 216
                    && (bytes[2] & 255) == 255;
        }

        if (type.equals("image/webp")) {

            return bytes.length >= 12
                    && new String(
                            bytes, 0, 4, StandardCharsets.US_ASCII
                    ).equals("RIFF")
                    && new String(
                            bytes, 8, 4, StandardCharsets.US_ASCII
                    ).equals("WEBP");
        }

        return false;
    }

    // 11. Find user by email
    private User user(String emailAddress) {

        return users.findByEmail(emailAddress)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found"));
    }

    // 12. Save complaint status history
    private void history(
            Complaint complaint,
            User user,
            String oldStatus,
            String newStatus,
            String remarks) {

        ComplaintHistory history = new ComplaintHistory();

        history.complaint = complaint;
        history.updatedBy = user;
        history.oldStatus = oldStatus;
        history.newStatus = newStatus;
        history.remarks = remarks;

        histories.save(history);
    }
}
