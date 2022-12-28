package com.example.entity;

import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Entity
@ToString

@Table(name = "profile")
public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name")
    private String fullname;

    @Column
    private String phone;

    @Column(name = "user_id")
    private Long userId;


}



