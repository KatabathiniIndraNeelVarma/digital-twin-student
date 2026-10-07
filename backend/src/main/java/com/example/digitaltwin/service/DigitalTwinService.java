package com.example.digitaltwin.service;
import com.example.digitaltwin.dto.DigitalTwinResponse;
import com.example.digitaltwin.repository.TwinReadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class DigitalTwinService {
    private final TwinReadRepository data;
    @Value("${app.twin.weights.academic}") private BigDecimal academicWeight;
    @Value("${app.twin.weights.attendance}") private BigDecimal attendanceWeight;
    @Value("${app.twin.weights.skills}") private BigDecimal skillsWeight;
    @Value("${app.twin.weights.learning}") private BigDecimal learningWeight;
    @Value("${app.twin.weights.projects}") private BigDecimal projectsWeight;
    @Value("${app.twin.weights.activities}") private BigDecimal activitiesWeight;
    @Value("${app.twin.classification.excellent}") private int excellent;
    @Value("${app.twin.classification.good}") private int good;
    @Value("${app.twin.classification.average}") private int average;
    @Value("${app.twin.classification.needs-improvement}") private int needsImprovement;
    @Value("${app.twin.attendance-risk}") private int attendanceRisk;
    @Value("${app.twin.academic-gap}") private int academicGap;
    public DigitalTwinService(TwinReadRepository data){this.data=data;}

    @org.springframework.transaction.annotation.Transactional(readOnly=true)
    public DigitalTwinResponse get(Long id){
        if(!data.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Student not found");
        var academic=bounded(data.academic(id)); var attendance=bounded(data.attendance(id));
        var skills=bounded(data.skills(id)); var learning=bounded(data.learning(id));
        var projectCount=Optional.ofNullable(data.projects(id)).orElse(0L); var activityCount=Optional.ofNullable(data.activities(id)).orElse(0L);
        var certificationCount=Optional.ofNullable(data.certifications(id)).orElse(0L);
        BigDecimal projects=projectCount==0?null:BigDecimal.valueOf(Math.min(100,projectCount*20));
        BigDecimal activities=activityCount==0?null:BigDecimal.valueOf(Math.min(100,activityCount*20));
        BigDecimal certifications=certificationCount==0?null:BigDecimal.valueOf(Math.min(100,certificationCount*20));
        var scores=Arrays.asList(new Score(academic,academicWeight),new Score(attendance,attendanceWeight),new Score(skills,skillsWeight),new Score(learning,learningWeight),new Score(projects,projectsWeight),new Score(activities,activitiesWeight));
        BigDecimal weightSum=scores.stream().filter(s->s.value!=null).map(Score::weight).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal sum=scores.stream().filter(s->s.value!=null).map(s->s.value.multiply(s.weight)).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal overall=weightSum.signum()==0?null:sum.divide(weightSum,2,RoundingMode.HALF_UP);
        String classification=overall==null?"INSUFFICIENT_DATA": overall.compareTo(BigDecimal.valueOf(excellent))>=0?"EXCELLENT":overall.compareTo(BigDecimal.valueOf(good))>=0?"GOOD":overall.compareTo(BigDecimal.valueOf(average))>=0?"AVERAGE":overall.compareTo(BigDecimal.valueOf(needsImprovement))>=0?"NEEDS_IMPROVEMENT":"AT_RISK";
        List<String> missing=new ArrayList<>(); if(academic==null)missing.add("academic performance");if(attendance==null)missing.add("attendance");if(skills==null)missing.add("skills");if(learning==null)missing.add("learning progress");if(projects==null)missing.add("projects");if(activities==null)missing.add("activities");
        List<DigitalTwinResponse.LearningGap> gaps=data.gaps(id,academicGap).stream().map(row->new DigitalTwinResponse.LearningGap((String)row[0],bounded((BigDecimal)row[1]),"Performance-based learning gap indicator")).toList();
        List<DigitalTwinResponse.RiskIndicator> risks=new ArrayList<>(); List<DigitalTwinResponse.Recommendation> recs=new ArrayList<>();
        if(attendance!=null&&attendance.compareTo(BigDecimal.valueOf(attendanceRisk))<0){String detail="Attendance is "+attendance+"%, below the configured "+attendanceRisk+"% threshold.";risks.add(new DigitalTwinResponse.RiskIndicator("ATTENDANCE","HIGH",detail));recs.add(new DigitalTwinResponse.Recommendation("ATTENDANCE","HIGH",detail,"Review missed sessions with the assigned mentor.","attendance"));}
        for(var gap:gaps){String detail=gap.subject()+" performance is "+gap.score()+"%, below "+academicGap+"%.";risks.add(new DigitalTwinResponse.RiskIndicator("ACADEMIC","MEDIUM",detail));recs.add(new DigitalTwinResponse.Recommendation("ACADEMIC","MEDIUM",detail,"Review the subject topics and arrange practice or mentor support.",gap.subject()));}
        if(skills==null) recs.add(new DigitalTwinResponse.Recommendation("SKILLS","LOW","No assessed skills are recorded.","Add skill assessments with evidence to build a career readiness view.","skills"));
        return new DigitalTwinResponse(id,data.name(id).orElse("Student"),overall,classification,
                new DigitalTwinResponse.Metrics(academic,attendance,skills,learning,projects,activities,certifications),missing,gaps,risks,recs);
    }
    private BigDecimal bounded(BigDecimal value){return value==null?null:value.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)).setScale(2,RoundingMode.HALF_UP);}
    private record Score(BigDecimal value,BigDecimal weight){}
}
