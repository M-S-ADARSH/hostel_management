package com.hostelcare.dto;
import jakarta.validation.constraints.*; import java.util.*;
public class ApiDtos {
 public record RegisterRequest(@NotBlank @Size(max=100) String name,@Email @NotBlank String email,@NotBlank @Size(min=8,max=72) String password,@Size(max=15) String phone,@NotNull Long roomId){}
 public record LoginRequest(@Email @NotBlank String email,@NotBlank String password){}
 public record AuthResponse(String token,Long userId,String name,String email,String role){}
 public record ComplaintRequest(@NotBlank @Size(max=150) String title,@NotBlank String description,@NotNull Long categoryId){}
 public record StatusRequest(@NotNull String status,String remarks){}
 public record PriorityRequest(@NotNull String priority){}
 public record AssignRequest(@NotNull Long staffId){}
 public record FeedbackRequest(@NotNull @Min(1) @Max(5) Integer rating,String comments){}
 public record CreateStaffRequest(@NotBlank String name,@Email @NotBlank String email,@NotBlank @Size(min=8,max=72) String password,@Size(max=15) String phone){}
 public record HostelRequest(@NotBlank String hostelName,String blockName,String address){}
 public record RoomRequest(@NotNull Long hostelId,@NotBlank String roomNumber,Integer floor,Integer capacity){}
 public record CategoryRequest(@NotBlank String categoryName,String description){}
}
