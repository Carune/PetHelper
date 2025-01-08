package com.pethelper.controller;

import com.pethelper.dto.LoginRequest;
import com.pethelper.dto.SignUpRequest;
import com.pethelper.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final AuthenticationSuccessHandler successHandler;

    @GetMapping("/login/oauth2/code/kakao")
    public void handleKakaoRedirect(HttpServletRequest request, HttpServletResponse response, 
                                  @RequestParam("code") String code, 
                                  Authentication authentication) throws Exception {
        // 인증 성공 후 홈으로 리다이렉트
        successHandler.onAuthenticationSuccess(request, response, authentication);
    }

    @GetMapping("/login/oauth2/code/google")
    public void handleGoogleRedirect(HttpServletRequest request, HttpServletResponse response, 
                                      @RequestParam("code") String code, 
                                      @RequestParam("state") String state, 
                                      Authentication authentication) throws Exception {
        // 인증 성공 후 홈으로 리다이렉트
        successHandler.onAuthenticationSuccess(request, response, authentication);
    }

    @GetMapping("/login/oauth2/code/naver")
    public void handleNaverRedirect(HttpServletRequest request, HttpServletResponse response, 
                                      @RequestParam("code") String code, 
                                      @RequestParam("state") String state, 
                                      Authentication authentication) throws Exception {
        // 인증 성공 후 홈으로 리다이렉트
        successHandler.onAuthenticationSuccess(request, response, authentication);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
} 