package com.hostelcare.entity;
import java.time.LocalDateTime;

 import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity @Table(name="complaint_attachments")
public class ComplaintAttachment
 { 
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="attachment_id") 
 public Long attachmentId; @ManyToOne(optional=false) @JoinColumn(name="complaint_id")
  public Complaint complaint; @Column(name="file_name",nullable=false) 
  public String fileName; @Column(name="file_path",nullable=false,length=500)
   public String filePath; @Column(name="uploaded_at") 
   public LocalDateTime uploadedAt=LocalDateTime.now(); 
}
