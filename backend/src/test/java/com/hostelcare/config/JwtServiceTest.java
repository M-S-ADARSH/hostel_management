package com.hostelcare.config;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
class JwtServiceTest {
 @Test void generatesAndValidatesSignedToken(){JwtService service=new JwtService();ReflectionTestUtils.setField(service,"secret","test-secret-key-that-is-at-least-thirty-two-bytes-long");ReflectionTestUtils.setField(service,"expiration",60000L);String token=service.generate("student@example.edu","STUDENT");assertNotNull(token);assertTrue(service.valid(token));assertEquals("student@example.edu",service.extractEmail(token));}
 @Test void rejectsMalformedToken(){JwtService service=new JwtService();ReflectionTestUtils.setField(service,"secret","test-secret-key-that-is-at-least-thirty-two-bytes-long");ReflectionTestUtils.setField(service,"expiration",60000L);assertFalse(service.valid("not-a-jwt"));}
}
