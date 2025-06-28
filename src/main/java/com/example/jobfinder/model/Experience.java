package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "experiences") // Tên bảng trong cơ sở dữ liệu
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@AttributeOverride(name = "name", column = @Column(name = "experience_name", unique = true, nullable = false, length = 100))
public class Experience extends BaseNameEntity{
    public Experience(){

    }
}
