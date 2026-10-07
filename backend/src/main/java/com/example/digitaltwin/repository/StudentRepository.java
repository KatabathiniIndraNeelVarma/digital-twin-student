package com.example.digitaltwin.repository;
import com.example.digitaltwin.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StudentRepository extends JpaRepository<Student,Long> { Optional<Student> findByUserEmailIgnoreCase(String email); boolean existsByStudentNumber(String number); }
