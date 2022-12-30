package com.example.admin.controller;

import com.example.admin.service.CookerService;
import com.example.admin.service.DeliveryService;
import com.example.dto.LocationMessageDTO;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
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
            if (text.equals("/start")) {
                user.setStep(null);
                menu(user.getChatId());
                return;
            }

            switch (text) {
                case Constant.finished -> {
                    user.setStep(Step.FINISHED_ORDER);
                    methodType(user.getChatId());
                }
                case Constant.notFinished -> {
                    user.setStep(Step.NOT_FINISHED_ORDER);
                    methodType(user.getChatId());
                }
                case Constant.searchOrder -> {
                    searchOrder(user.getChatId());
                    user.setStep(Step.SEARCH_ORDER);
                }
                case Constant.olibKetish -> {
                    olibketish(user.getChatId());
                }
                case Constant.yetkazish -> {
                    yetkazish(user.getChatId());
                }


                case Constant.back -> {
                    user.setStep(null);
                    menu(user.getChatId());
                }
            }


        }
    }

    private void yetkazish(Long chatId) {
        TelegramUsers user = saveUser(chatId);

        List<OrdersEntity> ordersList;

        OrdersStatus status = null;
        if (user.getStep().equals(Step.FINISHED_ORDER)) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("MARKDOWN");

            ordersList = ordersService.getListByStatusAndMethodType(OrdersStatus.FINISHED, MethodType.YETKAZIB_BERISH);
            ordersList.forEach(order -> {
                String text = ordersService.getOrderDetail(order);

                text += "\n*Mijoz:* _" + order.getProfile().getFullName() +
                        "_\n*Telefon raqam*: _" + order.getProfile().getPhone() + "_";

                sendMessage.setText(text);
                myTelegramBot.send(sendMessage);
            });
        } else if (user.getStep().equals(Step.NOT_FINISHED_ORDER)) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("MARKDOWN");
            ordersList = ordersService.getListByStatusAndMethodType(OrdersStatus.CONFIRMED, MethodType.YETKAZIB_BERISH);
            ordersList.forEach(order -> {

                        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                        markup.setKeyboard(keyboard);

                        LocationMessageDTO messageDTO = deliveryService.getLocationMessageDTO(chatId, order.getId());
                        if (messageDTO != null) {
                            DeleteMessage deleteMessage = new DeleteMessage();
                            deleteMessage.setChatId(chatId);
                            deleteMessage.setMessageId(messageDTO.getLocationMessageId());
                            deliveryService.deleteLocationMessageDTO(messageDTO);
                            myTelegramBot.send(deleteMessage);
                        }

                        List<InlineKeyboardButton> row = Button.location(order.getId(), false);
                        keyboard.add(row);

                        String text = ordersService.getOrderDetail(order);

                        text += "\n*Mijoz:* _" + order.getProfile().getFullName() +
                                "_\n*Telefon raqam*: _" + order.getProfile().getPhone() + "_";

                        sendMessage.setText(text);
                        sendMessage.setReplyMarkup(markup);
                        myTelegramBot.send(sendMessage);
                    }
            );


        }


    }

    private void olibketish(Long chatId) {
        TelegramUsers user = saveUser(chatId);

        List<OrdersEntity> ordersList;

        OrdersStatus status = null;
        if (user.getStep().equals(Step.FINISHED_ORDER)) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("MARKDOWN");

            ordersList = ordersService.getListByStatusAndMethodType(OrdersStatus.FINISHED, MethodType.OLIB_KETISH);
            ordersList.forEach(order -> {
                String text = ordersService.getOrderDetail(order);

                text += "\n*Mijoz:* _" + order.getProfile().getFullName() +
                        "_\n*Telefon raqam*: _" + order.getProfile().getPhone() + "_";

                sendMessage.setText(text);
                myTelegramBot.send(sendMessage);
            });
        } else if (user.getStep().equals(Step.NOT_FINISHED_ORDER)) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("MARKDOWN");
            ordersList = ordersService.getListByStatusAndMethodType(OrdersStatus.CONFIRMED, MethodType.OLIB_KETISH);
            ordersList.forEach(order -> {

                        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                        markup.setKeyboard(keyboard);

                        LocationMessageDTO messageDTO = deliveryService.getLocationMessageDTO(chatId, order.getId());
                        if (messageDTO != null) {
                            DeleteMessage deleteMessage = new DeleteMessage();
                            deleteMessage.setChatId(chatId);
                            deleteMessage.setMessageId(messageDTO.getLocationMessageId());
                            deliveryService.deleteLocationMessageDTO(messageDTO);
                            myTelegramBot.send(deleteMessage);
                        }

                        List<InlineKeyboardButton> row = Button.finish(order.getId());
                        List<InlineKeyboardButton> row2 = Button.location(order.getId(), false);
                        keyboard.add(row);
                        keyboard.add(row2);

                        String text = ordersService.getOrderDetail(order);

                        text += "\n*Mijoz:* _" + order.getProfile().getFullName() +
                                "_\n*Telefon raqam*: _" + order.getProfile().getPhone() + "_";

                        sendMessage.setText(text);
                        sendMessage.setReplyMarkup(markup);
                        myTelegramBot.send(sendMessage);
                    }
            );


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

    private void searchOrder(Long chatId) {
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
                                                Button.button(Constant.searchOrder)
                                        )
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
