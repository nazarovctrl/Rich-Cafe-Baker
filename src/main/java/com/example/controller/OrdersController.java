package com.example.controller;

import com.example.entity.MealEntity;
import com.example.entity.OrdersEntity;
import com.example.enums.OrdersStatus;
import com.example.enums.Step;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.AuthService;
import com.example.service.MealsService;
import com.example.service.OrderMealService;
import com.example.service.OrdersService;
import com.example.step.TelegramUsers;
import com.example.utill.SendMsg;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class OrdersController {

    public final MenuController menuController;
    private final MainController mainController;

    private final OrdersService ordersService;

    private final MealsService mealsService;
    private final OrderMealService orderMealService;

    private final FormalizationController formalizationController;

    private final MyTelegramBot myTelegramBot;


    private final OrdersMenuController ordersMenuController;
    private final AuthService authService;

    private OrdersEntity ordersEntity = new OrdersEntity();


    private List<TelegramUsers> usersList = new ArrayList<>();

    @Lazy
    public OrdersController(MenuController menuController, OrdersService ordersService, MealsService mealsService, MyTelegramBot myTelegramBot, MainController mainController, OrderMealService orderMealService, FormalizationController formalizationController, OrdersMenuController ordersMenuController, AuthService authService) {
        this.menuController = menuController;
        this.ordersService = ordersService;
        this.mealsService = mealsService;
        this.myTelegramBot = myTelegramBot;
        this.mainController = mainController;
        this.orderMealService = orderMealService;
        this.formalizationController = formalizationController;

        this.ordersMenuController = ordersMenuController;
        this.authService = authService;
    }

    public void addOrder(Message message) {


        TelegramUsers orders = saveUser(message.getChatId());

        TelegramUsers users = mainController.saveUser(message.getChatId());

        if (orders.getStep() == null) {
            orders.setStep(Step.MAIN);
        }

        if (message.hasText() && orders.getStep().equals(Step.MAIN)) {


            String text = message.getText();

            switch (text) {

                case Constant.savat -> {
                    //savatni quyish
                    formalizationController.ordersList(message);
                    orders.setStep(Step.SAVAT);
                }

                case Constant.back -> {
                    //orqaga
                    menuController.mainMenu(message);
                    users.setStep(Step.MAIN);

                }

                case Constant.home -> {
                    //bosh menu
                    menuController.mainMenu(message);
                    users.setStep(Step.MAIN);
                }


            }

            if (ordersService.isExists(message)) {
                ordersMenuController.findMenuName(message);
                orders.setMenu(message.getText());
                orders.setStep(Step.MEAL);
            }


            return;
        }

        if (orders.getStep().equals(Step.MEAL)) {

            String text = message.getText();
            switch (text) {

                case Constant.back -> {
                    menuController.orderMenu(message);
                    orders.setStep(Step.MAIN);
                }

                case Constant.home -> {
                    menuController.mainMenu(message);
                    users.setStep(Step.MAIN);
                }

            }

            orders.setMeal(message.getText());

            if (mealsService.isExists(message)) {
                ordersMenuController.findMenuMeal(message);
                orders.setStep(Step.CHOOSEMEAL);
                return;
            }
            return;
        }

        if (orders.getStep().equals(Step.CHOOSEMEAL)) {

            String text = message.getText();
            switch (text) {

                case Constant.back -> {
                    ordersMenuController.findMenuName(message, orders.getMenu());
                    orders.setStep(Step.MEAL);
                }

                case Constant.home -> {
                    menuController.mainMenu(message);
                    users.setStep(Step.MAIN);
                }

            }

            int count = 0;
            try {
                count = Integer.parseInt(message.getText());
            } catch (RuntimeException e) {
                myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                        "Miqdorni togri kiriting"));
                return;
            }

            if (count > 9) {
                myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                        "Miqdorni togri kiriting"));
                return;
            }

            ordersEntity.setCreatedDate(LocalDateTime.now());
            ordersEntity.setProfile(authService.findByUserId(message.getChatId()));
            ordersEntity.setStatus(OrdersStatus.NOT_CONFIRMED);


            ordersEntity = ordersService.add(ordersEntity);


            orderMealService.add(ordersEntity.getId(), getByName(orders.getMeal()), count);


            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Mahsulot savatchaga qoshildi, davom etamizmi?"));

            menuController.orderMenu(message);


            orders.setMeal(null);
            orders.setMenu(null);
        }


        if (orders.getStep().equals(Step.SAVAT)) {
            formalizationController.handle(message);
            return;
        }

        if (message.hasText()) {
            orders.setStep(null);
        }

    }

    public MealEntity getByName(String name) {
        Optional<MealEntity> optional = mealsService.findName(name);

        return optional.get();
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
