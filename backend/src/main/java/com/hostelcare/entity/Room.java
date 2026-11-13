package com.hostelcare.entity;
import jakarta.persistence.*;
@Entity @Table(name="rooms", uniqueConstraints=@UniqueConstraint(columnNames={"hostel_id","room_number"}))
public class Room { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long roomId; @ManyToOne(optional=false) @JoinColumn(name="hostel_id") public Hostel hostel; @Column(name="room_number",nullable=false,length=20) public String roomNumber; public Integer floor; public Integer capacity; }
