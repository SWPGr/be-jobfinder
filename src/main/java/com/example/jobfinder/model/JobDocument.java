package com.example.jobfinder.model;


import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.suggest.Completion;

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


}
