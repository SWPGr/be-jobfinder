package com.example.jobfinder.model;

import jakarta.persistence.*;

@Entity
@Table(name = "social_types")
public class SocialType extends BaseNameEntity {
    public SocialType() { super(); }
}