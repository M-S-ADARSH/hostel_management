package com.hostelcare.repository;
import com.hostelcare.entity.User; import com.hostelcare.enums.Role; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface UserRepository extends JpaRepository<User,Long> { Optional<User> findByEmail(String email); List<User> findByRoleAndActiveTrue(Role role); boolean existsByEmail(String email); }
