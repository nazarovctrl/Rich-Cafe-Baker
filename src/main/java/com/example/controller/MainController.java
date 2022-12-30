package com.example.controller;

import com.example.admin.service.SettingsService;
import com.example.entity.AdminEntity;
import com.example.entity.ProfileEntity;
import com.example.enums.Step;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.AuthService;
import com.example.service.UserService;
import com.example.step.TelegramUsers;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {
    private final MenuController menuController;
    private final MyTelegramBot myTelegramBot;
    private final SettingsService settingsService;
    private final OrdersController ordersController;
    private final AuthService authService;
    private final UserService userService;

    public Step step = Step.MAIN;

    List<TelegramUsers> usersList = new ArrayList<>();

    @Lazy
    public MainController(MenuController menuController, MyTelegramBot myTelegramBot, SettingsService settingsService, OrdersController ordersController, AuthService authService, UserService userService) {
        this.menuController = menuController;
        this.myTelegramBot = myTelegramBot;
        this.settingsService = settingsService;
        this.ordersController = ordersController;

        this.authService = authService;
        this.userService = userService;
    }


    public void start(Message message) {

        TelegramUsers users = saveUser(message.getChatId());
        if (message.hasText()) {
            String text = message.getText();

            if (text.equals("/start") || users.getStep() == null) {
                menuController.mainMenu(message);
                users.setStep(Step.MAIN);

            }

            if (users.getStep().equals(Step.COMMENT)) {
                if (text.equals(Constant.back)) {
                    users.setStep(Step.MAIN);
                    menuController.mainMenu(message);
                    return;
                }

                sendMessageToAdmin(text, message.getChatId());

            }

            if (users.getStep().equals(Step.MAIN)) {

                switch (text) {
                    case Constant.addOrder -> {
                        menuController.orderMenu(message);
                        users.setStep(Step.AddOrder);

                    }

                    case Constant.buyurtmalar -> {
                        menuController.order(message);

                    }
                    case Constant.settings -> {
                        //sozlanmalar
                        users.setStep(step = Step.SETTINGS);
                        menuController.settings(message);

                    }

                    case Constant.about -> {
                        //biz haqimizda
                        menuController.about(message);
                    }

                    case Constant.addComment -> {
                        comment(message);
                        users.setStep(Step.COMMENT);
                        return;
                    }

                }
                return;

            }

            if (users.getStep().equals(Step.AddOrder)) {

                ordersController.addOrder(message);

                return;
            }

            if (users.getStep().equals(Step.SETTINGS)) {
                switch (text) {

                    case Constant.phone -> {
                        //phone uzgartirish
                        menuController.contactButton(message);
                        users.setStep(Step.PHONE);

                    }
                    case Constant.back -> {
                        menuController.mainMenu(message);
                        users.setStep(Step.MAIN);
                    }
                }

            }


        }
    }

    private void sendMessageToAdmin(String text, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        ProfileEntity user = authService.findByUserId(chatId);

        sendMessage.setText("\uD83D\uDC64Mijoz: " + user.getFullName() +
                "\n\n\uD83D\uDCDETelefon raqam: " + user.getPhone() +
                "\n\n\uD83D\uDCDDFikr: \n" + text);


        List<AdminEntity> adminList = settingsService.getAdminList();
        adminList.forEach(admin -> {
            sendMessage.setChatId(admin.getUserId());
            myTelegramBot.send(sendMessage);
        });
        sendMessage.setText("✅Muvaffaqiyatli");
        sendMessage.setChatId(chatId);
        myTelegramBot.send(sendMessage);
    }

    private void comment(Message message) {
        myTelegramBot.send(
                SendMsg.sendMsg(
                        message.getChatId(),
                        "\uD83D\uDCCCHar bir fikringiz biz uchun muhim \n ✍️Fikringizni yozib qoldiring!",
                        Button.markup(
                                Button.rowList(
                                        Button.row(
                                                Button.button(Constant.back))

                                )
                        )
                )
        );
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

    public void changePhone(Message message) {
        TelegramUsers user = saveUser(message.getChatId());
        if (!user.getStep().equals(Step.PHONE)) {
            return;
        }


        userService.changePhone(user.getChatId(), message.getContact().getPhoneNumber());

        myTelegramBot.send(SendMsg.sendMsg(user.getChatId(), "\uD83D\uDCDE Telefon raqam o'zgartildi"));
        user.setStep(null);
        menuController.mainMenu(message);
    }
}
