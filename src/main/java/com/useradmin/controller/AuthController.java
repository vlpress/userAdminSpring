package com.useradmin.controller;

import com.useradmin.dto.LoginRequest;
import com.useradmin.service.AuthUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private PasswordEncoder passwordEncoder;
    private AuthUserDetailsService authUserDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String encryptedPassword = loginRequest.getPassword();

        UserDetails userDetails = authUserDetailsService.loadUserByUsername(loginRequest.getEmail());

        if (passwordEncoder.matches(encryptedPassword, userDetails.getPassword())) { // TODO: for JWT implementation
        //if (encryptedPassword.equals(userDetails.getPassword())) {
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("USER");

            Map<String, String> response = new HashMap<>();
            response.put("name", userDetails.getUsername());
            response.put("status", "authenticated");
            response.put("role", role);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

    }

}
