package com.example.digitaltwin.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name="students")
public class Student {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="user_id", nullable=false, unique=true) private UserAccount user;
    @Column(name="student_number", nullable=false, unique=true) private String studentNumber;
    @Column(name="full_name", nullable=false) private String fullName;
    @Column(name="department_id") private Long departmentId;
    private String program; private String cohort; private Integer semester;
    @Column(name="enrollment_date") private LocalDate enrollmentDate;
    protected Student() {}
    public Student(UserAccount user,String studentNumber,String fullName,String program,Integer semester){this.user=user;this.studentNumber=studentNumber;this.fullName=fullName;this.program=program;this.semester=semester;}
    public Long getId(){return id;} public String getFullName(){return fullName;} public String getStudentNumber(){return studentNumber;}
    public String getProgram(){return program;} public Integer getSemester(){return semester;} public UserAccount getUser(){return user;}
}
