package com.example.controller;

import com.example.entity.OrderMealEntity;

import com.example.entity.OrdersEntity;
import com.example.enums.MethodType;
import com.example.enums.Payment;
import com.example.enums.Step;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.OrderMealService;
import com.example.service.OrdersService;
import com.example.step.TelegramUsers;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Controller
public class FormalizationController {

    private final MyTelegramBot myTelegramBot;

    private final MenuController menuController;

    private final OrdersService ordersService;

    private final OrderMealService orderMealService;
    private final MainController mainController;

    private List<TelegramUsers> usersList = new ArrayList<>();

    @Lazy
    public FormalizationController(MyTelegramBot myTelegramBot, MenuController menuController, OrdersService ordersService, OrderMealService orderMealService, MainController mainController) {
        this.myTelegramBot = myTelegramBot;
        this.menuController = menuController;
        this.ordersService = ordersService;
        this.orderMealService = orderMealService;
        this.mainController = mainController;
    }

    public void handle(Message message) {

        TelegramUsers step = saveUser(message.getChatId());

        if (message.hasText()) {

            String text = message.getText();


            switch (text) {

                case Constant.tozalash -> {
                    deleteAllOrderByUserId(message.getChatId());
                    myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                            "Savatchangiz tozalandi"));
                    return;
                }

                case Constant.back -> {
                    if (step.getStep() == null || step.getStep().equals(Step.SAVAT)) {
                        menuController.orderMenu(message);
                        step.setStep(Step.MAIN);
                        return;
                    }
                    if (step.getStep().equals(Step.TYPE_METHOD)) {
                        typePayment(message);
                        step.setStep(Step.CASH);
                        return;
                    }
                    if (step.getStep().equals(Step.CASH)) {
                        ordersList(message);
                        step.setStep(Step.SAVAT);
                        return;
                    }

                    if (step.getStep().equals(Step.OLIB_KETISH) ||
                            step.getStep().equals(Step.DELIVERY)) {
                        typeMethod(message);
                        step.setStep(Step.TYPE_METHOD);
                        return;
                    }

                    if (step.getStep().equals(Step.CONFIRM)) {
                        delivery(message);
                        step.setStep(Step.DELIVERY);
                        return;
                    }

                    if (step.getStep().equals(Step.MAIN)) {
                        TelegramUsers telegramUsers = mainController.saveUser(message.getChatId());
                        telegramUsers.setStep(Step.MAIN);
                        menuController.mainMenu(message);
                        return;
                    }

                }

                case Constant.cash -> {
                    typeMethod(message);
                    step.setStep(Step.TYPE_METHOD);
                    ordersService.changePayment(message.getChatId(), Payment.CASH);
                    return;
                }
                case Constant.savat -> {
                    ordersList(message);
                    step.setStep(Step.SAVAT);
                    return;
                }
                case Constant.home -> {
                    TelegramUsers mainStep = mainController.saveUser(message.getChatId());
                    mainStep.setStep(Step.MAIN);
                    menuController.mainMenu(message);
                    step.setStep(null);
                    return;
                }

            }


            if (message.hasText()) {
                String text1 = message.getText();
                switch (text1) {
                    case Constant.yetkazish -> {
                        delivery(message);
                        ordersService.changeMethodType(message.getChatId(), MethodType.YETKAZIB_BERISH);
                        step.setStep(Step.DELIVERY);
                        return;
                    }
                    case Constant.olibKetish -> {
                        olibKetish(message);
                        ordersService.changeMethodType(message.getChatId(), MethodType.OLIB_KETISH);
                        step.setStep(Step.OLIB_KETISH);
                        return;
                    }
                    case Constant.formalization -> {
                        typePayment(message);
                        step.setStep(Step.CASH);
                        return;
                    }
                    case Constant.confirm -> {
                        checkOrder(message);
                        step.setStep(null);
                        return;
                    }
                }


            }


        }


    }

    private void checkOrder(Message message) {
        /// TODO send to admin for checking orders


    }

    private void olibKetish(Message message) {

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(message.getChatId());
        InputFile inputFile = new InputFile();
        File file = new File("attach/address.png");
        inputFile.setMedia(file);

        sendPhoto.setPhoto(inputFile);


        String str = "\uD83D\uDCCD Olib ketish uchun manzil : ";

        str += "\n Qarshi shahri, 3-mikrorayon, Yubileyniy tabaka orqa tomoni";
        str += "\n <a href=\"https://www.google.com/maps/place/Rich+bakery+uz/@38.8433167,65.8029988,17z/data=!3m1!4b1!4m5!3m4!1s0x3f4ea902e3ececbb:0x6e7ba51076d43d34!8m2!3d38.8433125!4d65.8051875\">xaritadan ko'rish</a>";


        sendPhoto.setCaption(str);
        sendPhoto.setParseMode("HTML");

        myTelegramBot.send(sendPhoto);
        confirm(message);

    }


    private void confirm(Message message) {
        TelegramUsers step = saveUser(message.getChatId());
        List<OrderMealEntity> oderMealList = orderMealService.getNOTCONFIRMEDOderMealList(message.getChatId());
        OrdersEntity orders = ordersService.getByUserId(message.getChatId());

        if (oderMealList.isEmpty() || orders == null) {
            System.out.println("order");
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Sizning savatingiz bo'sh \n, buyurtma berish uchun mahsulot tanlang"));
            menuController.orderMenu(message);
            return;
        }

        if (orders.getPayment() == null) {
            typePayment(message);
            step.setStep(Step.CASH);
            return;
        }
        if (orders.getMethodType() == null) {
            typeMethod(message);
            step.setStep(Step.TYPE_METHOD);
            return;
        }


        String savat = "\uD83D\uDCE5 *Buyurtma :* \n\n";

        for (OrderMealEntity entity : oderMealList) {

            savat += entity.getMeal().getName() + "\n" +
                    entity.getQuantity() + " x " + entity.getMeal().getPrice() + " = " +
                    entity.getMeal().getPrice() * entity.getQuantity() + "\n\n";


        }
        savat += "*Buyurtma turi:* ";

        if (orders.getMethodType().equals(MethodType.OLIB_KETISH)) {
            savat += "_Olib ketish_";
        } else {
            savat += "\uD83D\uDEF5 _Yetkazib berish_";
        }

        savat += "\n *To'lov turi:* ";

        if (orders.getPayment() == Payment.CASH) {
            savat += "\uD83D\uDCB5 _Naqd_";
        }


        myTelegramBot.send(
                SendMsg.sendMsgParse(
                        message.getChatId(),
                        savat,
                        Button.markup(
                                Button.rowList(
                                        Button.row(Button.button(Constant.confirm)),
                                        Button.row(Button.button(Constant.back), Button.button(Constant.cancel))
                                )
                        )
                )
        );


    }

    private void delivery(Message message) {

        String str = "";

        myTelegramBot.send(
                SendMsg.sendMsg(
                        message.getChatId(),
                        "Yetkazib berish manzilini jonating",
                        Button.markup(
                                Button.rowList(
                                        Button.row(Button.location()),
                                        Button.row(Button.button(Constant.back))
                                )

                        )

                )
        );

    }

    private void deleteAllOrderByUserId(Long chatId) {
        ordersService.deleteByUserId(chatId);
    }


    public void typePayment(Message message) {
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "To`lov turini yuboring",
                Button.markup(Button.rowList(
                        Button.row(Button.button(Constant.cash)),
                        Button.row(Button.button(Constant.back))
                ))));
    }

    public void typeMethod(Message message) {
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Yetkazib berish turini tanlang",
                Button.markup(Button.rowList(
                        Button.row(Button.button(Constant.olibKetish),
                                Button.button(Constant.yetkazish)),
                        Button.row(Button.button(Constant.back))
                ))));
    }

    public void ordersList(Message message) {

        List<OrderMealEntity> oderMealList = orderMealService.getNOTCONFIRMEDOderMealList(message.getChatId());

        if (oderMealList.isEmpty()) {
            System.out.println("order");
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Sizning savatingiz bo'sh, buyurtma berish uchun mahsulot tanlang"));
            menuController.orderMenu(message);
        }

        String savat = "\uD83D\uDCE5 Savat: \n\n";

        for (OrderMealEntity entity : oderMealList) {

            savat += entity.getMeal().getName() + "\n" +
                    entity.getQuantity() + " x " + entity.getMeal().getPrice() + " = " +
                    entity.getMeal().getPrice() * entity.getQuantity() + "\n\n";


        }

        myTelegramBot.send(
                SendMsg.sendMsg(
                        message.getChatId(),
                        savat,
                        Button.markup(
                                Button.rowList(
                                        Button.row(Button.button(Constant.formalization)),
                                        Button.row(Button.button(Constant.back), Button.button(Constant.tozalash))
                                )
                        )
                )
        );


//        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
//                "\uD83D\uDCE5 Savat: \n\n" +
//                        entity.getMeal_table().getName() + "\n" +
//                        entity.getQuantity() + " x " + entity.getMeal_table().getPrice() + " = " +
//                        entity.getMeal_table().getPrice() * entity.getQuantity(),
//                Button.markup(Button.rowList(Button.row(
//                                Button.button(Constant.back),
//                                Button.button(Constant.tozalash)
//                        ),
//                        Button.row(Button.button(Constant.formalization))
//                ))
//        ));
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

    public void setLocationToOrder(Message message) {
        TelegramUsers step = saveUser(message.getChatId());
        if (!step.getStep().equals(Step.DELIVERY)) {
            return;
        }

        Location location = message.getLocation();
        ordersService.setLocation(message.getChatId(), location.getLatitude(), location.getLongitude());
        confirm(message);

        step.setStep(Step.CONFIRM);
    }
}
