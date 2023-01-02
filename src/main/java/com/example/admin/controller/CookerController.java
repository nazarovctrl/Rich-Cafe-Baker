package com.example.admin.controller;

import com.example.admin.service.CookerService;
import com.example.admin.service.DeliveryService;
import com.example.entity.OrdersEntity;
import com.example.enums.MethodType;
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
public class CookerController {
    private final CookerService cookerService;
    private final MyTelegramBot myTelegramBot;
    private final OrdersService ordersService;
    private final DeliveryService deliveryService;
    private List<TelegramUsers> usersList = new ArrayList<>();


    @Lazy
    public CookerController(CookerService cookerService, MyTelegramBot myTelegramBot, OrdersService ordersService, DeliveryService deliveryService) {
        this.cookerService = cookerService;
        this.myTelegramBot = myTelegramBot;
        this.ordersService = ordersService;
        this.deliveryService = deliveryService;
    }


    public void handle(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            TelegramUsers user = saveUser(message.getChatId());
            if (text.equals("/start") || text.equals("Asosiy Menyu !")) {
                user.setStep(null);
                menu(user.getChatId());
                return;
            }

            switch (text) {
                case Constant.finished -> {
                    user.setStep(Step.FINISHED_ORDER);
                    methodType(user.getChatId());
                    return;
                }
                case Constant.notFinished -> {
                    user.setStep(Step.NOT_FINISHED_ORDER);
                    methodType(user.getChatId());
                    return;
                }
                case Constant.searchOrder -> {
                    searchOrderMenu(user.getChatId());
                    user.setStep(Step.SEARCH_ORDER);
                    return;
                }
                case Constant.save -> {
                    save(user.getChatId());
                    return;
                }
                case Constant.olibKetish -> {
                    olibketish(user.getChatId());
                    return;
                }
                case Constant.yetkazish -> {
                    yetkazish(user.getChatId());
                    return;
                }


                case Constant.back -> {
                    user.setStep(null);
                    menu(user.getChatId());
                }
            }

            if (Step.SEARCH_ORDER.equals(user.getStep())) {
                searchOrder(text, user.getChatId());
                return;
            }
            menu(user.getChatId());
        }
    }

    private void save(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("MARKDOWN");

        List<OrdersEntity> orderList = ordersService.getListByStatus(OrdersStatus.CHECKING);
        orderList.forEach(order ->
                cookerService.sendCheckingOrder(sendMessage, order, chatId)
        );

    }

    private void searchOrder(String text, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("MARKDOWN");


        int number;
        sendMessage.setText("Buyurtma raqami xato");

        try {
            number = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            myTelegramBot.send(sendMessage);
            return;
        }

        OrdersEntity order = ordersService.findByIdConfirmedOrder(number);
        if (order == null) {
            sendMessage.setText(number + " bunday raqamli buyurtma topilmadi");
            myTelegramBot.send(sendMessage);
            return;
        }

        String message = "Buyurtma holati: ";

        if (order.getStatus() == OrdersStatus.FINISHED) {
            message += "Yakunlangan \uD83D\uDFE2";
            cookerService.sendOrderWithDetail(message, sendMessage, order, null);
            return;
        }

        if (order.getStatus() == OrdersStatus.CANCELLED) {
            message += "Bekor qilingan ❌";
            cookerService.sendOrderWithDetail(message, sendMessage, order, null);
            return;
        }

        if (order.getStatus() == OrdersStatus.CHECKING) {
            cookerService.sendCheckingOrder(sendMessage, order, chatId);
            return;
        }

        // status==Confirmed
        message += "Qabul qilingan \uD83D\uDFE1";
        if (order.getMethodType().equals(MethodType.OLIB_KETISH)) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            markup.setKeyboard(keyboard);
            List<InlineKeyboardButton> row = Button.finish(order.getId());
            keyboard.add(row);
            cookerService.sendOrderWithDetail(message, sendMessage, order, markup);
            return;
        }
        cookerService.deliveryNotFinishedOrder(message, chatId, order, sendMessage);


    }

    private void yetkazish(Long chatId) {

        TelegramUsers user = saveUser(chatId);

        if (user.getStep().equals(Step.FINISHED_ORDER)) {
            cookerService.getFinishedOrderList(chatId, MethodType.YETKAZIB_BERISH);
            return;
        }

        if (user.getStep().equals(Step.NOT_FINISHED_ORDER)) {
            cookerService.getDeliveryNotFinishedOrderList(chatId);
        }


    }

    private void olibketish(Long chatId) {
        TelegramUsers user = saveUser(chatId);

        if (user.getStep().equals(Step.FINISHED_ORDER)) {
            cookerService.getFinishedOrderList(chatId, MethodType.OLIB_KETISH);
            return;
        }
        if (user.getStep().equals(Step.NOT_FINISHED_ORDER)) {
            cookerService.olibketishNotFinishedOrderList(chatId);
        }

    }


    private void methodType(Long chatId) {
        myTelegramBot.send(
                SendMsg.sendMsg(
                        chatId,
                        "Buyurtma turini tanlang",
                        Button.markup(
                                Button.rowList(
                                        Button.row(
                                                Button.button(Constant.yetkazish),
                                                Button.button(Constant.olibKetish)
                                        ),

                                        Button.row(
                                                Button.button(Constant.back)
                                        )
                                )
                        )
                )
        );
    }

    private void searchOrderMenu(Long chatId) {
        myTelegramBot.send(
                SendMsg.sendMsg(
                        chatId,
                        "\uD83D\uDCE6 Buyurtma raqamini kiriting ",
                        Button.markup(
                                Button.rowList(
                                        Button.row(
                                                Button.button(Constant.back))
                                )
                        )
                )
        );
    }

    private void menu(Long chatId) {
        myTelegramBot.send(
                SendMsg.sendMsg(
                        chatId,
                        "Asosiy menu",
                        Button.markup(
                                Button.rowList(
                                        Button.row(
                                                Button.button(Constant.finished),
                                                Button.button(Constant.notFinished)
                                        ),

                                        Button.row(
                                                Button.button(Constant.searchOrder),
                                                Button.button(Constant.save))
                                )
                        )
                )
        );

    }

    private TelegramUsers saveUser(Long chatId) {
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
