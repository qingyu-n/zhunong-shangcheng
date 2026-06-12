package com.zhunong.mall;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    
    @Test
    public void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String encoded = encoder.encode(password);
        System.out.println("BCrypt encoded password for '" + password + "':");
        System.out.println(encoded);
        
        // Verify
        boolean matches = encoder.matches(password, encoded);
        System.out.println("Verification: " + matches);
        
        // Also verify the old one
        String oldEncoded = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO";
        boolean oldMatches = encoder.matches(password, oldEncoded);
        System.out.println("Old password verification: " + oldMatches);
    }
}
