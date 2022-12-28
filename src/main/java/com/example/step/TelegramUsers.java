package com.example.step;

import com.example.enums.Step;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class TelegramUsers {

    private Long chatId;

    private Step step;

    private String meal;

    private String menu;

    public TelegramUsers() {
    }


}
