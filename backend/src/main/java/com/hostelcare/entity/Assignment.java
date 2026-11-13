package com.hostelcare.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="assignments")
public class Assignment { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="assignment_id") public Long assignmentId; @ManyToOne(optional=false) @JoinColumn(name="complaint_id") public Complaint complaint; @ManyToOne(optional=false) @JoinColumn(name="staff_id") public User staff; @ManyToOne(optional=false) @JoinColumn(name="assigned_by") public User assignedBy; @Column(name="assigned_at",nullable=false) public LocalDateTime assignedAt=LocalDateTime.now(); @Column(name="is_active",nullable=false) public boolean active=true; }
