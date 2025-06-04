package com.example.jobfinder.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;


@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseNameEntity{
    private String name;
}
