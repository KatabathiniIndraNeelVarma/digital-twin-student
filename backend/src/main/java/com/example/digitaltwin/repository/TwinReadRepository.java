package com.example.digitaltwin.repository;
import com.example.digitaltwin.entity.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface TwinReadRepository extends Repository<Student,Long> {
    boolean existsById(Long id);
    @Query("select s.fullName from Student s where s.id=:id") Optional<String> name(@Param("id") Long id);
    @Query(value="select 100 * sum(r.marks) / nullif(sum(a.max_marks),0) from assessment_results r join assessments a on a.id=r.assessment_id where r.student_id=:id",nativeQuery=true) BigDecimal academic(@Param("id") Long id);
    @Query(value="select 100 * sum(case when status in ('PRESENT','LATE') then 1 else 0 end) / nullif(sum(case when status <> 'EXCUSED' then 1 else 0 end),0) from attendance where student_id=:id",nativeQuery=true) BigDecimal attendance(@Param("id") Long id);
    @Query(value="select avg(score) from student_skills where student_id=:id",nativeQuery=true) BigDecimal skills(@Param("id") Long id);
    @Query(value="select avg(progress) from learning_goals where student_id=:id",nativeQuery=true) BigDecimal learning(@Param("id") Long id);
    @Query(value="select count(*) from projects where student_id=:id",nativeQuery=true) Long projects(@Param("id") Long id);
    @Query(value="select count(*) from activities where student_id=:id",nativeQuery=true) Long activities(@Param("id") Long id);
    @Query(value="select count(*) from certifications where student_id=:id",nativeQuery=true) Long certifications(@Param("id") Long id);
    @Query(value="select s.name,100*sum(r.marks)/nullif(sum(a.max_marks),0) as score from assessment_results r join assessments a on a.id=r.assessment_id join subjects s on s.id=a.subject_id where r.student_id=:id group by s.id,s.name having score < :threshold",nativeQuery=true) java.util.List<Object[]> gaps(@Param("id") Long id,@Param("threshold") int threshold);
}
