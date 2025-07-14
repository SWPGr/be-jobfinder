package com.example.jobfinder.model;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import com.fasterxml.jackson.annotation.JsonInclude;

@Document(indexName = "users")
@JsonInclude(JsonInclude.Include.ALWAYS)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String fullName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String companyName;

    @Field(type = FieldType.Keyword)
    private String location;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String website;

    @Field(type = FieldType.Keyword)
    private String phone;

    @Field(type = FieldType.Keyword)
    private String avatarUrl;

    @Field(type = FieldType.Keyword)
    private String banner;

    @Field(type = FieldType.Keyword)
    private String teamSize;

    @Field(type = FieldType.Integer)
    private Integer yearOfEstablishment;

    @Field(type = FieldType.Keyword)
    private String mapLocation;

    @Field(type = FieldType.Long)
    private Long roleId;

    @Field(type = FieldType.Long)
    private Long educationId;

    @Field(type = FieldType.Long)
    private Long organizationId;

    @Field(type = FieldType.Boolean)
    @Builder.Default
    private Boolean isPremium = false;

    @Field(type = FieldType.Integer)
    @Builder.Default
    private Integer verified = 0;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String createdAt;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String updatedAt;

    @Field(type = FieldType.Integer)
    @Builder.Default
    private Integer jobsPosted = 0;

    @Field(type = FieldType.Float)
    @Builder.Default
    private Float averageRating = 0.0f;

    @Field(type = FieldType.Integer)
    @Builder.Default
    private Integer totalReviews = 0;
}
