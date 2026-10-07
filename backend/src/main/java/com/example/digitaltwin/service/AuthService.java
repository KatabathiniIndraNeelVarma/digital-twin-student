package com.example.digitaltwin.service;

import com.example.digitaltwin.dto.*;
import com.example.digitaltwin.entity.*;
import com.example.digitaltwin.repository.*;
import com.example.digitaltwin.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users; private final StudentRepository students;
    private final PasswordEncoder encoder; private final AuthenticationManager auth; private final JwtService jwt;
    @Value("${app.jwt.expiration-ms}") private long expiry;
    public AuthService(UserRepository users,StudentRepository students,PasswordEncoder encoder,AuthenticationManager auth,JwtService jwt){this.users=users;this.students=students;this.encoder=encoder;this.auth=auth;this.jwt=jwt;}
    @Transactional
    public AuthResponse register(RegisterRequest request){
        String email=request.email().trim().toLowerCase();
        if(users.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("An account with this email already exists");
        if(students.existsByStudentNumber(request.studentNumber().trim())) throw new IllegalArgumentException("Student number is already registered");
        UserAccount user=users.save(new UserAccount(email,encoder.encode(request.password()),Role.STUDENT));
        Student student=students.save(new Student(user,request.studentNumber().trim(),request.fullName().trim(),request.program(),request.semester()));
        return response(user,student.getId());
    }
    public AuthResponse login(LoginRequest request){
        var authenticated=auth.authenticate(new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(),request.password()));
        UserAccount user=(UserAccount)authenticated.getPrincipal();
        Long studentId=students.findByUserEmailIgnoreCase(user.getEmail()).map(Student::getId).orElse(null);
        return response(user,studentId);
    }
    private AuthResponse response(UserAccount user,Long studentId){return new AuthResponse(jwt.issue(user),"Bearer",expiry/1000,user.getEmail(),user.getRole().name(),studentId);}
}
