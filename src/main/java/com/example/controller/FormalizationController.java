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
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FormalizationController {

    private final MyTelegramBot myTelegramBot;

    private final MenuController menuController;

    private final OrdersService ordersService;

    private final OrderMealService orderMealService;

    private List<TelegramUsers> usersList = new ArrayList<>();

    @Lazy
    public FormalizationController(MyTelegramBot myTelegramBot, MenuController menuController, OrdersService ordersService, OrderMealService orderMealService) {
        this.myTelegramBot = myTelegramBot;
        this.menuController = menuController;
        this.ordersService = ordersService;
        this.orderMealService = orderMealService;
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

            }


            if (message.hasText()) {
                String text1 = message.getText();
                switch (text1) {
                    case Constant.yetkazish -> {
                        delivery(message);
                        ordersService.changeMethodType(message.getChatId(), MethodType.YETKAZIB_BERISH);
                        return;
                    }
                    case Constant.olibKetish -> {
                         olibKetish(message);
                        ordersService.changeMethodType(message.getChatId(), MethodType.OLIB_KETISH);
                        return;
                    }
                    case Constant.formalization -> {
                        typePayment(message);
                        step.setStep(Step.CASH);
                        return;
                    }
                }


            }


        }


    }

    private void olibKetish(Message message) {

    }

    private void delivery(Message message) {

        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setRequestLocation(true);
        keyboardButton.setText("Manzilni jo'natish");

        KeyboardRow row = new KeyboardRow();
        row.add(keyboardButton);

        List<KeyboardRow> rowList = new ArrayList<>();
        rowList.add(row);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(rowList);
        replyKeyboardMarkup.setResizeKeyboard(true);

        myTelegramBot.send(SendMsg.sendMsgMark(message.getChatId(), "Yetkazib berish manzilini jonating", replyKeyboardMarkup));

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

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                savat,
                Button.markup(Button.rowList(Button.row(
                                Button.button(Constant.back),
                                Button.button(Constant.tozalash)
                        ),
                        Button.row(Button.button(Constant.formalization))
                ))
        ));


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
}
