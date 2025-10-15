// src/main/java/com/example/mat/dto/MatIssueDTO.java
package com.yedam.scm.dto;

import lombok.Data;
import java.util.List;

@Data
public class MatIssueRegisterRequest {
    private String workOrderNo; 
    private String workDate;   
    private String productId;
    private String productName;
    private Long   plDetId;     
    private List<Line> lines;

    @Data
    public static class Line {
        private String matId;   
        private Double issueQty; 
        private String unit;    
    }
}
