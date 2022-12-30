package com.example.admin.controller;

import com.example.admin.service.SupplierService;
import com.example.entity.OrdersEntity;
import com.example.enums.OrdersStatus;
import com.example.enums.Step;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.OrdersService;
import com.example.step.TelegramUsers;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SupplierController {

    private final SupplierService supplierService;
    private final OrdersService ordersService;
    private final MyTelegramBot myTelegramBot;

    @Lazy
    public SupplierController(SupplierService supplierService, OrdersService ordersService, MyTelegramBot myTelegramBot) {
        this.supplierService = supplierService;
        this.ordersService = ordersService;
        this.myTelegramBot = myTelegramBot;
    }

    private final List<TelegramUsers> usersList = new ArrayList<>();


    public void handle(Message message) {
        if (message.hasText()) {
            Long userId = message.getChatId();

            TelegramUsers user = saveUser(userId);

            String text = message.getText();

            if (text.equals("/start")) {
                user.setStep(null);
                menu(message);
                return;
            }

            switch (text) {
                case Constant.holat -> {
                    user.setStep(Step.HOLAT);
                    holat(userId);
                    return;
                }
                case Constant.delivered -> {
                    orderHistoryList(userId);
                    return;
                }
                case Constant.notDelivered -> {
                    confirmedOrderHistoryList(userId);
                    return;
                }
                case Constant.back -> {
                    if (user.getStep() == Step.HOLAT) {
                        user.setStep(null);
                        menu(message);
                        return;
                    }
                }
            }


            if (Step.HOLAT.equals(user.getStep())) {

                switch (text) {
                    case Constant.busy -> changeStatus(userId, true);
                    case Constant.empty -> changeStatus(userId, false);
                }
            }

        }

    }

    private void confirmedOrderHistoryList(Long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.setParseMode("MARKDOWN");


        List<OrdersEntity> oderList = ordersService.getListBySupplierUserId(userId, OrdersStatus.CONFIRMED);
        oderList.forEach(order -> {

            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            markup.setKeyboard(keyboard);
            List<InlineKeyboardButton> row1 = Button.finish(order.getId());
            List<InlineKeyboardButton> row2 = Button.location(order.getId(), true);
            keyboard.add(row1);
            keyboard.add(row2);
            sendMessage.setReplyMarkup(markup);

            String text = ordersService.getOrderDetail(order);
            sendMessage.setText(text);

            myTelegramBot.send(sendMessage);

        });

    }

    private void orderHistoryList(Long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.setParseMode("MARKDOWN");

        List<OrdersEntity> oderList = ordersService.getListBySupplierUserId(userId, OrdersStatus.FINISHED);
        oderList.forEach(order -> {
            String text = ordersService.getOrderDetail(order);
            sendMessage.setText(text);
            myTelegramBot.send(sendMessage);
        });

    }

    private void changeStatus(Long userId, boolean busy) {
        supplierService.changeStatus(userId, busy);
        holat(userId);
    }

    private void holat(Long userId) {
        boolean b = supplierService.getByUserId(userId).getBusy();

        String buttonText;
        String messageText = "\uD83D\uDD16 Sizning holatingiz: ";
        if (b) {
            messageText += Constant.busy;
            buttonText = Constant.empty;
        } else {
            messageText += Constant.empty;
            buttonText = Constant.busy;
        }

        myTelegramBot.send(
                SendMsg.sendMsgMark(
                        userId,
                        messageText,
                        Button.markup(
                                Button.rowList(
                                        Button.row(
                                                Button.button(buttonText)
                                        ),
                                        Button.row(
                                                Button.button(Constant.back)
                                        )
                                )
                        )
                )
        );

    }

    private void menu(Message message) {
        myTelegramBot.send(
                SendMsg.sendMsg(
                        message.getChatId(),
                        "Asosiy menu",
                        Button.markup(
                                Button.rowList(
                                        Button.row(
                                                Button.button(Constant.holat),
                                                Button.button(Constant.delivered)
                                        ),
                                        Button.row(
                                                Button.button(Constant.notDelivered)
                                        )
                                )
                        )
                )
        );

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


}
