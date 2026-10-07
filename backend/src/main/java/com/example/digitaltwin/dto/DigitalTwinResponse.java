package com.example.digitaltwin.dto;
import java.math.BigDecimal;
import java.util.List;
public record DigitalTwinResponse(Long studentId,String studentName,BigDecimal overallScore,String classification,
                                  Metrics metrics,List<String> missingData,List<LearningGap> learningGaps,
                                  List<RiskIndicator> riskIndicators,List<Recommendation> recommendations) {
    public record Metrics(BigDecimal academic,BigDecimal attendance,BigDecimal skills,BigDecimal learning,
                          BigDecimal projects,BigDecimal activities,BigDecimal certifications){}
    public record LearningGap(String subject,BigDecimal score,String indicator){}
    public record RiskIndicator(String category,String priority,String detail){}
    public record Recommendation(String category,String priority,String reason,String action,String relatedMetric){}
}
