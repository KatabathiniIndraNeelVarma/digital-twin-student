package com.example.digitaltwin.controller;
import com.example.digitaltwin.dto.DigitalTwinResponse;
import com.example.digitaltwin.service.DigitalTwinService;
import com.example.digitaltwin.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/students")
public class DigitalTwinController {
    private final DigitalTwinService service;
    private final StudentRepository students;
    public DigitalTwinController(DigitalTwinService service,StudentRepository students){this.service=service;this.students=students;}
    @GetMapping("/{studentId}/digital-twin")
    public DigitalTwinResponse get(@PathVariable Long studentId,Authentication authentication){
        boolean admin=authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
        boolean student=authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_STUDENT"));
        if(student && !students.findByUserEmailIgnoreCase(authentication.getName()).map(s->s.getId().equals(studentId)).orElse(false)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Students can only access their own Digital Twin");
        }
        if(!admin && !student) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Teacher assignment access is not available until assignment is configured");
        return service.get(studentId);
    }
}
