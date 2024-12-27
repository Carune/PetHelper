package com.pethelper.service;

import com.pethelper.domain.Member;
import com.pethelper.dto.LoginRequest;
import com.pethelper.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    
    @Override
    public ResponseEntity<?> signUp(SignUpRequest request) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("이미 가입된 이메일입니다.");
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .authProvider(Member.AuthProvider.LOCAL)
                .build();
                
        memberRepository.save(member);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        // 인증 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        
        // 실제 검증 (사용자 비밀번호 체크)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        
        // 인증 정보를 기반으로 JWT 토큰 생성
        String token = tokenService.createToken(authentication);
        
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body("로그인이 완료되었습니다.");
    }
} 