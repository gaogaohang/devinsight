package com.devinsight.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectVO {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerName;
    private String myRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
