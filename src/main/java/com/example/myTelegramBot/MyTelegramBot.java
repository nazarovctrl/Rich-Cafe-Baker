package com.example.myTelegramBot;

import com.example.admin.controller.AdminController;
import com.example.config.BotConfig;
import com.example.controller.AuthController;
import com.example.controller.FormalizationController;
import com.example.controller.MainController;

import com.example.step.TelegramUsers;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {


    private final MainController mainController;
    private final AuthController authController;
    private final AdminController adminController;
    private final FormalizationController formalizationController;
    private final BotConfig botConfig;

    private List<TelegramUsers> usersList = new ArrayList<>();


    public MyTelegramBot(MainController mainController, AuthController authController, AdminController adminController, FormalizationController formalizationController, BotConfig botConfig) {
        this.mainController = mainController;
        this.authController = authController;
        this.adminController = adminController;
        this.formalizationController = formalizationController;

        this.botConfig = botConfig;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()) {
//            adminController.handleCalback(update.getCallbackQuery());
        }

        if (update.hasMessage()) {


            Message message = update.getMessage();

            Long userId = message.getChatId();


            TelegramUsers users = saveUser(message.getChatId());


            if (message.hasContact() && !authController.isExists(message)) {
                authController.handle(message);
            }


            if (message.hasText()) {
                if (!authController.isExists(message)) {
                    authController.handle(message);
                    return;
                }
                mainController.start(message);
            }

            if (message.hasLocation()) {
                formalizationController.setLocationToOrder(message);
                return;
            }


        }


    }


    public Message send(SendMessage sendMessage) {
        try {
            return execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(SendVideo sendVideo) {
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public TelegramUsers saveUser(Long chatId) {

        TelegramUsers user = usersList.stream().filter(u -> u.getChatId().equals(chatId)).findAny().orElse(null);
        if (user != null) {
            return user;
        }

//        for (TelegramUsers users : usersList) {
//            if (users.getChatId().equals(chatId)) {
//                return users;
//            }
//        }


        TelegramUsers users = new TelegramUsers();
        users.setChatId(chatId);
        usersList.add(users);

        return users;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


}
