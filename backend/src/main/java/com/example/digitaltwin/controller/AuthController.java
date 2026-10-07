package com.example.digitaltwin.controller;
import com.example.digitaltwin.dto.*;
import com.example.digitaltwin.service.AuthService;
import com.example.digitaltwin.dto.CurrentUserResponse;
import com.example.digitaltwin.repository.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;
    private final StudentRepository students;
    public AuthController(AuthService service,StudentRepository students){this.service=service;this.students=students;}
    @PostMapping("/register") public AuthResponse register(@Valid @RequestBody RegisterRequest request){return service.register(request);}
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request){return service.login(request);}
    @GetMapping("/me") public CurrentUserResponse current(Authentication authentication){
        String role=authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_","");
        Long studentId=students.findByUserEmailIgnoreCase(authentication.getName()).map(s->s.getId()).orElse(null);
        return new CurrentUserResponse(authentication.getName(),role,studentId);
    }
}
