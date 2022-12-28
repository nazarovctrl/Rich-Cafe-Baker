package com.example.dto;

import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ProfileDTO {


    private Integer id;

    private String name;

    private String userName;

    private String phone;

    private Long userId;

    private String lang;

    private UserStatus status;

    private UserRole role;

}
