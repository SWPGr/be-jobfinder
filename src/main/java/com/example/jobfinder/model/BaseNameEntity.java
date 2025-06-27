// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\model\BaseNameEntity.java
package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // GIỮ LẠI
@AllArgsConstructor(access = AccessLevel.PROTECTED) // GIỮ LẠI
@SuperBuilder
@ToString(callSuper = true)
public abstract class BaseNameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

}