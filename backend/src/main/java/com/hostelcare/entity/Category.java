package com.hostelcare.entity;
import jakarta.persistence.*;
@Entity @Table(name="categories")
public class Category { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="category_id") public Long categoryId; @Column(name="category_name",nullable=false,unique=true,length=100) public String categoryName; @Column(length=255) public String description; }
