package com.example.jobfinder.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "jobs")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String location;

    @Field(type = FieldType.Long)
    private Long employerId;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Long)
    private Long jobLevelId;

    @Field(type = FieldType.Long)
    private Long jobTypeId;

    @Field(type = FieldType.Long)
    private Long educationId;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Integer)
    private Integer applicantCount;

    @Field(type = FieldType.Boolean)
    @Builder.Default
    private Boolean isSave = false;
}
