package com.hostelcare.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="complaint_history")
public class ComplaintHistory { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="history_id") public Long historyId; @ManyToOne(optional=false) @JoinColumn(name="complaint_id") public Complaint complaint; @ManyToOne @JoinColumn(name="updated_by") public User updatedBy; @Column(name="old_status",length=30) public String oldStatus; @Column(name="new_status",nullable=false,length=30) public String newStatus; @Column(columnDefinition="TEXT") public String remarks; @Column(name="changed_at",nullable=false) public LocalDateTime changedAt=LocalDateTime.now(); }
