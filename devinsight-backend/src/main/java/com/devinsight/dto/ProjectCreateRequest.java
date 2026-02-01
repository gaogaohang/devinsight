package com.devinsight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectCreateRequest {
    
    @NotBlank(message = "项目名称不能为空")
    @Size(min = 1, max = 100, message = "项目名称长度必须在1-100之间")
    private String name;
    
    @Size(max = 500, message = "项目描述不能超过500字")
    private String description;
}
