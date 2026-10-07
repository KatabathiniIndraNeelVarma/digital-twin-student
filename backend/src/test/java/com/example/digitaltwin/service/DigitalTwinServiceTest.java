package com.example.digitaltwin.service;

import com.example.digitaltwin.repository.TwinReadRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class DigitalTwinServiceTest {
    private DigitalTwinService service(Map<String,Object> values) {
        TwinReadRepository repository=(TwinReadRepository)Proxy.newProxyInstance(
                TwinReadRepository.class.getClassLoader(),new Class[]{TwinReadRepository.class},(proxy,method,args)->{
                    if(method.getName().equals("existsById")) return true;
                    if(method.getName().equals("name")) return Optional.of("Test Student");
                    if(method.getName().equals("gaps")) return List.of();
                    return values.get(method.getName());
                });
        DigitalTwinService twin=new DigitalTwinService(repository);
        ReflectionTestUtils.setField(twin,"academicWeight",new BigDecimal("0.35"));
        ReflectionTestUtils.setField(twin,"attendanceWeight",new BigDecimal("0.15"));
        ReflectionTestUtils.setField(twin,"skillsWeight",new BigDecimal("0.20"));
        ReflectionTestUtils.setField(twin,"learningWeight",new BigDecimal("0.15"));
        ReflectionTestUtils.setField(twin,"projectsWeight",new BigDecimal("0.10"));
        ReflectionTestUtils.setField(twin,"activitiesWeight",new BigDecimal("0.05"));
        ReflectionTestUtils.setField(twin,"excellent",90); ReflectionTestUtils.setField(twin,"good",80);
        ReflectionTestUtils.setField(twin,"average",65); ReflectionTestUtils.setField(twin,"needsImprovement",50);
        ReflectionTestUtils.setField(twin,"attendanceRisk",75); ReflectionTestUtils.setField(twin,"academicGap",60);
        return twin;
    }
    private Map<String,Object> values(Object academic,Object attendance,Object skills,Object learning){
        var values=new java.util.HashMap<String,Object>(); values.put("academic",academic); values.put("attendance",attendance);
        values.put("skills",skills); values.put("learning",learning); values.put("projects",0L);
        values.put("activities",0L); values.put("certifications",0L); return values;
    }
    @Test void normalizesScoreToAvailableDataAndReportsMissingDimensions(){
        var input=values(new BigDecimal("80"),null,new BigDecimal("70"),null);
        var twin=service(withNulls(input,"attendance","learning")).get(7L);
        assertEquals(new BigDecimal("76.36"),twin.overallScore()); assertEquals("AVERAGE",twin.classification());
        assertTrue(twin.missingData().contains("attendance")); assertNull(twin.metrics().attendance());
    }
    @Test void missingAllMetricsDoesNotInventZeroScore(){
        var twin=service(withNulls(values(null,null,null,null),"academic","attendance","skills","learning")).get(8L);
        assertNull(twin.overallScore()); assertEquals("INSUFFICIENT_DATA",twin.classification()); assertTrue(twin.missingData().size()>=4);
    }
    @Test void lowAttendanceCreatesRiskAndRecommendation(){
        var twin=service(withNulls(values(new BigDecimal("90"),new BigDecimal("68"),null,null),"skills","learning")).get(9L);
        assertTrue(twin.riskIndicators().stream().anyMatch(r->r.category().equals("ATTENDANCE")));
        assertTrue(twin.recommendations().stream().anyMatch(r->r.relatedMetric().equals("attendance")));
    }
    private Map<String,Object> withNulls(Map<String,Object> map,String... keys){
        var copy=new java.util.HashMap<>(map); for(String key:keys)copy.put(key,null); return copy;
    }
}
