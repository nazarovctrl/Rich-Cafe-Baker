package com.example.myTelegramBot;


import com.example.admin.controller.AdminMainController;
import com.example.admin.controller.SupplierController;
import com.example.admin.service.SettingsService;
import com.example.admin.service.SupplierService;
import com.example.config.BotConfig;
import com.example.controller.AuthController;
import com.example.controller.CallBackQueryController;
import com.example.controller.FormalizationController;
import com.example.controller.MainController;

import com.example.step.TelegramUsers;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {


    private final MainController mainController;
    private final AuthController authController;
    private final FormalizationController formalizationController;
    private final BotConfig botConfig;
    private final AdminMainController adminController;
    private final CallBackQueryController callBackQueryController;
    private final SettingsService settingsService;
    private final SupplierService supplierService;
    private final SupplierController supplierController;

    private List<TelegramUsers> usersList = new ArrayList<>();

    public MyTelegramBot(MainController mainController, AuthController authController, FormalizationController formalizationController, BotConfig botConfig, AdminMainController adminController, CallBackQueryController callBackQueryController, SettingsService settingsService, SupplierService supplierService, SupplierController supplierController) {
        this.mainController = mainController;
        this.authController = authController;
        this.formalizationController = formalizationController;
        this.botConfig = botConfig;
        this.adminController = adminController;
        this.callBackQueryController = callBackQueryController;
        this.settingsService = settingsService;
        this.supplierService = supplierService;
        this.supplierController = supplierController;
    }


    @Override
    public void onUpdateReceived(Update update) {


        if (update.hasMessage()) {

            Message message = update.getMessage();

            Long userId = message.getChatId();

            if (settingsService.isAdmin(userId)) {
                adminController.handle(message);
                return;
            }

            if (supplierService.isSupplier(userId)) {
                supplierController.handle(message);
                return;
            }


            saveUser(message.getChatId());


            if (message.hasContact() && !authController.isExists(message)) {
                authController.handle(message);
            }

            if (message.hasContact()){
                mainController.changePhone(message);
                return;
            }


            if (message.hasText()) {
                if (!authController.isExists(message)) {
                    authController.handle(message);
                    return;
                }
                mainController.start(message);
                return;
            }

            if (message.hasLocation()) {
                formalizationController.setLocationToOrder(message);
                return;
            }


        }
        if (update.hasCallbackQuery()) {
            callBackQueryController.handleCallbackQuery(update);
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

    public void send(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(EditMessageReplyMarkup editMessage) {
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Message send(SendLocation location) {
        try {
            return execute(location);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public TelegramUsers saveUser(Long chatId) {

        TelegramUsers user = usersList.stream().filter(u -> u.getChatId().equals(chatId)).findAny().orElse(null);
        if (user != null) {
            return user;
        }

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
