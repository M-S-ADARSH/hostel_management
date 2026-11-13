package com.hostelcare.repository; 
import java.util.List;

 import org.springframework.data.jpa.repository.JpaRepository;

 import com.hostelcare.entity.ComplaintAttachment; 
 public interface ComplaintAttachmentRepository extends JpaRepository<ComplaintAttachment,Long>
  { List<ComplaintAttachment> findByComplaintComplaintId(Long id); }
