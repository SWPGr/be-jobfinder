package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "social_types")
@AttributeOverride(name = "name", column = @Column(name = "social_type_name", unique = true, nullable = false))
public class SocialType extends BaseNameEntity {
    public SocialType() { }

    // Một SocialType có thể có nhiều UserSocialType
    @OneToMany(mappedBy = "socialType", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("socialType-userSocialTypes")
    private Set<UserSocialType> userSocialTypes = new HashSet<>();
}