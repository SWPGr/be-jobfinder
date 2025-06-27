package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "experiences") // Tên bảng trong cơ sở dữ liệu
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // ✅ Bắt buộc với entity
@AllArgsConstructor
@AttributeOverride(name = "name", column = @Column(name = "experience_name", unique = true, nullable = false, length = 100))
public class Experience extends BaseNameEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
