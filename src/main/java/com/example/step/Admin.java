package com.example.step;

import com.example.enums.Step;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Admin {
    private Long chatId;

    private Step step;
    private String phone;
}
