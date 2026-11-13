package com.hostelcare.service;
import com.hostelcare.config.JwtService; import com.hostelcare.dto.ApiDtos.*; import com.hostelcare.entity.*; import com.hostelcare.enums.Role; import com.hostelcare.repository.*; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service public class AuthService {
 private final UserRepository users; private final RoomRepository rooms; private final PasswordEncoder encoder; private final JwtService jwt;
 public AuthService(UserRepository u,RoomRepository r,PasswordEncoder e,JwtService j){users=u;rooms=r;encoder=e;jwt=j;}
 @Transactional public AuthResponse register(RegisterRequest r){if(users.existsByEmail(r.email().toLowerCase()))throw new IllegalArgumentException("Email is already registered."); User u=new User();u.name=r.name().trim();u.email=r.email().toLowerCase().trim();u.password=encoder.encode(r.password());u.phone=r.phone();u.role=Role.STUDENT;u.room=rooms.findById(r.roomId()).orElseThrow(()->new NoSuchElementException("Room not found"));users.save(u);return response(u);}
 public AuthResponse login(LoginRequest r){User u=users.findByEmail(r.email().toLowerCase()).orElseThrow(()->new IllegalArgumentException("Invalid email or password."));if(!u.active||!encoder.matches(r.password(),u.password))throw new IllegalArgumentException("Invalid email or password.");return response(u);}
 private AuthResponse response(User u){return new AuthResponse(jwt.generate(u.email,u.role.name()),u.userId,u.name,u.email,u.role.name());}
}
