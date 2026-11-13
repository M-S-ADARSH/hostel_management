package com.hostelcare.entity;
import com.hostelcare.enums.Role; import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="users")
public class User { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="user_id") public Long userId; @Column(nullable=false,length=100) public String name; @Column(nullable=false,unique=true,length=150) public String email; @Column(nullable=false) public String password; @Column(length=15) public String phone; @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) public Role role; @ManyToOne @JoinColumn(name="room_id") public Room room; public boolean active=true; @Column(name="created_at",nullable=false) public LocalDateTime createdAt=LocalDateTime.now(); }
