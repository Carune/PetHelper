package com.pethelper.service;

import com.pethelper.dto.SignUpRequest;
import com.pethelper.dto.LoginRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> signUp(SignUpRequest request);
    ResponseEntity<?> login(LoginRequest request);
} 