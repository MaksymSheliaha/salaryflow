package com.msk.salaryflow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
@Getter
public class DepartmentSearchRequest {
    private Pageable pageable;
    private String searchTerm;
    private boolean employeeInfo;

    public String cacheKey(){
        StringBuilder builder = new StringBuilder();
        builder.append("term:").append(searchTerm == null ? "" : searchTerm.toLowerCase());
        builder.append(";empInfo:").append(employeeInfo);
        if(pageable!=null){
            builder.append(";page:")
                    .append(pageable.getPageNumber())
                    .append(";size:")
                    .append(pageable.getPageSize())
                    .append(";sort:")
                    .append(pageable.getSort());
        }
        return builder.toString();
    }
}
