package com.hostelcare.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="feedback")
public class Feedback { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="feedback_id") public Long feedbackId; @OneToOne(optional=false) @JoinColumn(name="complaint_id",unique=true) public Complaint complaint; @ManyToOne(optional=false) @JoinColumn(name="student_id") public User student; @Column(nullable=false) public Integer rating; @Column(columnDefinition="TEXT") public String comments; @Column(name="submitted_at") public LocalDateTime submittedAt=LocalDateTime.now(); }
