package com.github.kzhunmax.jobsearch.model;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Resume extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;
}
