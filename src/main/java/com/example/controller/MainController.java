package com.example.controller;

import com.example.enums.Step;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.AddMenuService;
import com.example.step.TelegramUsers;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {
    private final MenuController menuController;


    private final MyTelegramBot myTelegramBot;

    private final OrdersController ordersController;



    public Step step = Step.MAIN;

    List<TelegramUsers> usersList = new ArrayList<>();

    @Lazy
    public MainController(MenuController menuController, MyTelegramBot myTelegramBot, OrdersController ordersController ) {
        this.menuController = menuController;
        this.myTelegramBot = myTelegramBot;
        this.ordersController = ordersController;

    }


    public void start(Message message) {


        TelegramUsers users = saveUser(message.getChatId());
        if (message.hasText()) {
            String text = message.getText();

            if (text.equals("/start") || users.getStep() == null) {
                menuController.mainMenu(message);
                users.setStep(Step.MAIN);

            }

            if (users.getStep().equals(Step.MAIN)) {

                switch (text) {
                    case Constant.addOrder -> {
                        menuController.orderMenu(message);
//                        addMenuService.menuList(message);
                        users.setStep(Step.AddOrder);

                    }

                    case Constant.buyurtmalar -> {
                        menuController.order(message);
//                        step = StepStatus.ORDER;

                    }
                    case Constant.settings -> {
                        //sozlanmalar
                        menuController.settings(message);
                        users.setStep(step = Step.SETTINGS);

                    }

                    case Constant.about -> {
                        //biz haqimizda
                        menuController.about(message);
                    }

                    case Constant.addComment -> {
                        //fikr qoldirish
                    }

//                    default -> {
//                        SendMessage sendMessage = new SendMessage();
//                        sendMessage.setChatId(message.getChatId());
//                        sendMessage.setText("Mavjud emas");
//
//                        myTelegramBot.send(sendMessage);
//                    }
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
                        break;
                    }
                    case Constant.back -> {
                        menuController.mainMenu(message);
                        users.setStep(Step.MAIN);
                    }
                }

            }



        }
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
