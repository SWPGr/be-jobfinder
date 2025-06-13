package com.example.jobfinder.model;

import jakarta.persistence.*;

@Entity
@Table(name = "social_types")
@AttributeOverride(name = "name", column = @Column(name = "social_type_name", unique = true, nullable = false))
public class SocialType extends BaseNameEntity {
    public SocialType() { super(); }
}