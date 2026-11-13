package com.hostelcare.entity;
import jakarta.persistence.*;
@Entity @Table(name="hostels")
public class Hostel { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long hostelId; @Column(nullable=false,length=100) public String hostelName; @Column(length=50) public String blockName; @Column(length=255) public String address; }
