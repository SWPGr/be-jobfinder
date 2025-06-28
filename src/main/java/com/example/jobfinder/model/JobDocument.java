package com.example.jobfinder.model;


import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "jobs")
@Getter
@Setter
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
    private long EmployerId;

    @Field(type = FieldType.Keyword)
    private String Category;

    @Field(type = FieldType.Keyword)
    private String JobLevel;

    @Field(type = FieldType.Keyword)
    private String JobType;

    @Field(type = FieldType.Integer)
    private int ViewCount = 0;

    @Field(type = FieldType.Integer)
    private int applicantCount = 0;


}
