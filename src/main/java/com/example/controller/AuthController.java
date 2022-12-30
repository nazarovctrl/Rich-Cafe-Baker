package com.example.controller;

import com.example.entity.ProfileEntity;
import com.example.enums.Step;
import com.example.enums.UserStatus;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.AuthService;

import com.example.step.TelegramUsers;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;


@Controller
public class AuthController {

    private final AuthService authService;

    private final MyTelegramBot myTelegramBot;

    private final MenuController menuController;

    private List<TelegramUsers> usersList = new ArrayList<>();
    ProfileEntity profileEntity = new ProfileEntity();

    @Lazy
    public AuthController(AuthService authService, MyTelegramBot myTelegramBot, MenuController menuController) {
        this.authService = authService;
        this.myTelegramBot = myTelegramBot;
        this.menuController = menuController;
    }


    public void handle(Message message) {


        TelegramUsers users = saveUser(message.getChatId());

        if (message.hasText()) {

            if (users.getStep() == null) {
                myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                        "Assalomu alaykum Ismingiz va Familiyangizni kiriting"));

                users.setStep(Step.NAME);
                return;
            }

            if (users.getStep().equals(Step.NAME)) {
                profileEntity.setFullName(message.getText());

                myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                        "Iltimos raqamingizni yuboring",
                        Button.markup(Button.rowList(Button.row(
                                Button.button()
                        )))));
                users.setStep(Step.PHONE);
                return;
            }

            if (users.getStep().equals(Step.PHONE)) {

                if (!checkPhone(message.getText())) {
                    myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                            "Iltimos telefon raqamni to'g'ri kiriting ! "));
                    return;
                }

                if (checkPhoneExists(message.getText())) {
                    myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                            "Bu raqam ro'yxatdan o'tgan \n" +
                                    "Iltimos qaytadan kiriting "));
                    return;
                }

                profileEntity.setPhone(message.getText());
                profileEntity.setUserId(message.getChatId());
                profileEntity.setStatus(UserStatus.ACTIVE);


                authService.createProfile(profileEntity);
                myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                        "Muvaffaqiyatli saqlandi"));

                profileEntity = new ProfileEntity();
                menuController.mainMenu(message);
                users.setStep(null);
            }

            return;
        }

        if (message.hasContact()) {

            if (users.getStep().equals(Step.PHONE)) {

                if (checkPhoneExists(message.getContact().getPhoneNumber())) {
                    myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                            "Bu raqam avval kiritilgan "));
                    return;
                }

                profileEntity.setPhone(message.getContact().getPhoneNumber());
                profileEntity.setUserId(message.getChatId());
                profileEntity.setStatus(UserStatus.ACTIVE);

                authService.createProfile(profileEntity);

                myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                        "Muvaffaqiyatli saqlandi"));
                menuController.mainMenu(message);

                profileEntity = new ProfileEntity();
                users.setStep(null);

            }
        }


    }


    public boolean isExists(Message message) {
        return authService.isExists(message.getChatId());
    }

    public boolean checkPhone(String text) {
        if (text.startsWith("+998") && text.length() == 13 || text.startsWith("998") && text.length() == 12) {

            return true;
        }
        return false;
    }

    public boolean checkPhoneExists(String text) {
        return authService.isExists(text);
    }

    public TelegramUsers saveUser(Long chatId) {

        for (TelegramUsers users : usersList) {
            if (users.getChatId().equals(chatId)) {
                return users;
            }
        }


        TelegramUsers users = new TelegramUsers();
        users.setChatId(chatId);
        usersList.add(users);

        return users;
    }
}
