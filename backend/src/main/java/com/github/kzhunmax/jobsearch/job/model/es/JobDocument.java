package com.github.kzhunmax.jobsearch.job.model.es;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "jobs")
public class JobDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String company;

    @Field(type = FieldType.Keyword)
    private String location;

    @Field(type = FieldType.Double)
    private Double salary;

    @Field(type = FieldType.Boolean)
    private Boolean active;
}
