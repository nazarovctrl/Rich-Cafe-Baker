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

@Table(name = "admin_table")
public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name")
    private String fullname;

    @Column
    private String phone;

    @Column
    private String password;

    @Column(name = "user_id")
    private Long userId;
    @Enumerated(EnumType.STRING)
    @Column
    private UserRole status;


}



