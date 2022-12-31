package com.example.admin.controller;

import com.example.admin.service.CookerService;
import com.example.admin.service.DeliveryService;
import com.example.admin.service.SupplierService;
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
public class SupplierController {

    private final SupplierService supplierService;
    private final OrdersService ordersService;
    private final MyTelegramBot myTelegramBot;
    private final DeliveryService deliveryService;
    private final CookerService cookerService;

    @Lazy
    public SupplierController(SupplierService supplierService, OrdersService ordersService, MyTelegramBot myTelegramBot, DeliveryService deliveryService, CookerService cookerService) {
        this.supplierService = supplierService;
        this.ordersService = ordersService;
        this.myTelegramBot = myTelegramBot;
        this.deliveryService = deliveryService;
        this.cookerService = cookerService;
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
                case Constant.save -> {
                    notGivenOrderList(userId);
                    return;
                }
                case Constant.searchOrder -> {
                    searchOrderMenu(user.getChatId());
                    user.setStep(Step.SEARCH_ORDER);
                    return;
                }
                case Constant.back -> {
                    user.setStep(null);
                    menu(message);
                    return;

                }
            }


            if (Step.HOLAT.equals(user.getStep())) {
                switch (text) {
                    case Constant.busy -> changeStatus(userId, true);
                    case Constant.empty -> changeStatus(userId, false);
                }
                return;
            }

            if (Step.SEARCH_ORDER.equals(user.getStep())) {
                searchOrder(text, user.getChatId());
                return;
            }

            menu(message);

        }

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

        OrdersEntity order = ordersService.findByIdAndSupplierUserIdConfirmedOrder(number, chatId);


        OrdersEntity order2 = ordersService.findByIdConfirmedOrder(number);

        if (order == null && order2 == null) {
            sendMessage.setText(number + " Siz qabul qilgan buyurtmalar orasida bunday raqamli buyurtma topilmadi");
            myTelegramBot.send(sendMessage);
            return;
        }

        if (order2.getStatus().equals(OrdersStatus.CHECKING)) {
            sendMessage.setReplyMarkup(Button.deliveryMarkup(order2.getId()));
            text = ordersService.getOrderDetail(order2);
            text += "\n*Mijoz:* _" + order2.getProfile().getFullName() +
                    "_\n*Telefon raqam*: _" + order2.getProfile().getPhone() + "_";
            sendMessage.setText(text);
            myTelegramBot.send(sendMessage);
            return;
        }

        if (order == null) {
            sendMessage.setText(number + " Siz qabul qilgan buyurtmalar orasida bunday raqamli buyurtma topilmadi");
            myTelegramBot.send(sendMessage);
            return;
        }



        String message = "Buyurtma holati: ";

        if (order.getStatus() == OrdersStatus.FINISHED) {
            message += "Yakunlangan \uD83D\uDFE2";
            cookerService.sendOrderWithDetail(message, sendMessage, order, null);
            return;
        }

        deliveryService.searchAndDeleteAndRemoveLocationMessageDTO(chatId, order.getId());


        // status==Confirmed
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        List<InlineKeyboardButton> row1 = Button.finish(order.getId());
        List<InlineKeyboardButton> row2 = Button.location(order.getId(), true);
        keyboard.add(row1);
        keyboard.add(row2);

        cookerService.sendOrderWithDetail("", sendMessage, order, markup);

    }

    private void notGivenOrderList(Long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.setParseMode("MARKDOWN");
        List<OrdersEntity> orderList = ordersService.getListByStatusAndMethodType(OrdersStatus.CHECKING, MethodType.YETKAZIB_BERISH);


        orderList.forEach(order ->
                {
                    sendMessage.setReplyMarkup(Button.deliveryMarkup(order.getId()));
                    String text = ordersService.getOrderDetail(order);
                    text += "\n*Mijoz:* _" + order.getProfile().getFullName() +
                            "_\n*Telefon raqam*: _" + order.getProfile().getPhone() + "_";
                    sendMessage.setText(text);
                    deliveryService.searchAndDeleteAndRemoveLocationMessageDTO(userId, order.getId());
                    myTelegramBot.send(sendMessage);
                }
        );

    }

    private void confirmedOrderHistoryList(Long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.setParseMode("MARKDOWN");


        List<OrdersEntity> oderList = ordersService.getListBySupplierUserId(userId, OrdersStatus.CONFIRMED);
        oderList.forEach(order -> {
                    deliveryService.searchAndDeleteAndRemoveLocationMessageDTO(userId, order.getId());
                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    markup.setKeyboard(keyboard);
                    List<InlineKeyboardButton> row1 = Button.finish(order.getId());
                    List<InlineKeyboardButton> row2 = Button.location(order.getId(), true);
                    keyboard.add(row1);
                    keyboard.add(row2);

                    cookerService.sendOrderWithDetail("", sendMessage, order, markup);
                }
        );

    }

    private void orderHistoryList(Long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.setParseMode("MARKDOWN");
        String text = "Buyurtma holati: Yakunlangan \uD83D\uDFE2";
        List<OrdersEntity> oderList = ordersService.getListBySupplierUserId(userId, OrdersStatus.FINISHED);
        oderList.forEach(order -> cookerService.sendOrderWithDetail(text, sendMessage, order, null));

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
                                                Button.button(Constant.notDelivered),
                                                Button.button(Constant.save)
                                        ),
                                        Button.row(
                                                Button.button(Constant.searchOrder)
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
