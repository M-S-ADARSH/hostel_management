package com.hostelcare.config;
import com.hostelcare.entity.*;import com.hostelcare.enums.Role;import com.hostelcare.repository.*;import org.springframework.beans.factory.annotation.Value;import org.springframework.boot.CommandLineRunner;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration public class BootstrapData {
 @Bean CommandLineRunner initialize(HostelRepository hostels,RoomRepository rooms,CategoryRepository categories,UserRepository users,PasswordEncoder encoder,@Value("${ADMIN_EMAIL:}") String adminEmail,@Value("${ADMIN_PASSWORD:}") String adminPassword,@Value("${ADMIN_NAME:HostelCare Administrator}") String adminName){return args->{
  if(categories.count()==0){for(String n:new String[]{"Electrical","Plumbing","Cleaning","Furniture","Internet","Civil Maintenance","Other"}){Category c=new Category();c.categoryName=n;categories.save(c);}}
  Hostel hostel=hostels.findAll().stream().findFirst().orElseGet(()->{Hostel h=new Hostel();h.hostelName="Main Hostel";h.blockName="Block A";h.address="Update this address in administration";return hostels.save(h);});
  if(rooms.count()==0){for(int i=101;i<=110;i++){Room r=new Room();r.hostel=hostel;r.roomNumber=String.valueOf(i);r.floor=i/100;r.capacity=3;rooms.save(r);}}
  if(adminEmail!=null&&!adminEmail.isBlank()&&adminPassword!=null&&!adminPassword.isBlank()&&!users.existsByEmail(adminEmail.toLowerCase())){User a=new User();a.name=adminName;a.email=adminEmail.toLowerCase();a.password=encoder.encode(adminPassword);a.role=Role.ADMIN;users.save(a);}
 };}
}
