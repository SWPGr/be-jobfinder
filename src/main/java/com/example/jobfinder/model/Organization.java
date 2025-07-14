package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "organizations")
@Setter
@Getter
@SuperBuilder
@ToString(callSuper = true)
@AttributeOverride(name = "name", column = @Column(name = "organization_type", unique = true, nullable = false, length = 50))
public class Organization extends BaseNameEntity{
    public Organization() {

    }

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    @JsonBackReference("organization-userDetails")
    private Set<UserDetail> userDetails = new HashSet<>();
}
