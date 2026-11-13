package com.hostelcare.config;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.http.HttpMethod; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.http.SessionCreationPolicy; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; import org.springframework.web.cors.*; import java.util.List;
@Configuration public class SecurityConfig {
 @Value("${app.frontend-origin}") String origin;
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtAuthenticationFilter jwt)throws Exception{
  return http.csrf(c->c.disable()).cors(c->c.configurationSource(corsConfigurationSource())).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a->a
   .requestMatchers("/api/auth/register","/api/auth/login","/api/public/**","/error").permitAll()
   .requestMatchers("/api/auth/me").authenticated()
   .requestMatchers("/api/admin/**").hasRole("ADMIN")
   .requestMatchers("/api/staff/**").hasAnyRole("STAFF","ADMIN")
   .requestMatchers("/api/student/**").hasAnyRole("STUDENT","ADMIN")
   .requestMatchers(HttpMethod.GET,"/api/complaints/*/attachments/**").authenticated()
   .requestMatchers("/api/complaints/**").authenticated().anyRequest().denyAll()).addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
 }
 @Bean CorsConfigurationSource corsConfigurationSource(){CorsConfiguration c=new CorsConfiguration();c.setAllowedOrigins(List.of(origin,"http://127.0.0.1:5500","http://localhost:5173"));c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type"));c.setExposedHeaders(List.of("Content-Disposition"));UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}
}
