package com.example.admin.service;

import com.example.admin.repository.AdminRepository;
import com.example.dto.LocationMessageDTO;
import com.example.entity.AdminEntity;
import com.example.entity.OrdersEntity;
import com.example.enums.MethodType;
import com.example.enums.OrdersStatus;
import com.example.enums.UserRole;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.OrdersService;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class CookerService {

    private final AdminRepository adminRepository;
    private final MyTelegramBot myTelegramBot;
    private final OrdersService ordersService;
    private final DeliveryService deliveryService;

    @Lazy
    public CookerService(AdminRepository adminRepository, MyTelegramBot myTelegramBot, OrdersService ordersService, DeliveryService deliveryService) {
        this.adminRepository = adminRepository;
        this.myTelegramBot = myTelegramBot;
        this.ordersService = ordersService;
        this.deliveryService = deliveryService;
    }


    public List<AdminEntity> getCookerList() {
        return adminRepository.findByRole(UserRole.COOKER);
    }

    public boolean isCooker(Long userId) {
        return adminRepository.existsByUserIdAndRole(userId,UserRole.COOKER);
    }

    public void getFinishedOrderList(Long chatId, MethodType methodType) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("MARKDOWN");

        List<OrdersEntity> ordersList = ordersService.getListByStatusAndMethodType(OrdersStatus.FINISHED, methodType);
        ordersList.forEach(order -> {
            String text = "Buyurtma holati: Yakunlangan \uD83D\uDFE2";

            text += ordersService.getOrderDetail(order);

            text += "\n*Mijoz:* _" + order.getProfile().getFullName() +
                    "_\n*Telefon raqam*: _" + order.getProfile().getPhone() + "_";

            sendMessage.setText(text);
            myTelegramBot.send(sendMessage);
        });
    }

    public void getDeliveryNotFinishedOrderList(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("MARKDOWN");
        String text = "Buyurtma holati: Qabul qilingan \uD83D\uDFE1";

        List<OrdersEntity> ordersList = ordersService.getListByStatusAndMethodType(OrdersStatus.CONFIRMED,
                MethodType.YETKAZIB_BERISH);

        ordersList.forEach(order -> deliveryNotFinishedOrder(text, chatId, order, sendMessage));

    }

    public void deliveryNotFinishedOrder(String text, Long chatId, OrdersEntity order, SendMessage sendMessage) {
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

        sendOrderWithDetail(text, sendMessage, order, markup);
    }

    public void olibketishNotFinishedOrderList(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("MARKDOWN");

        String text = "Buyurtma holati: Qabul qilingan \uD83D\uDFE1";
        List<OrdersEntity> ordersList = ordersService.getListByStatusAndMethodType(OrdersStatus.CONFIRMED, MethodType.OLIB_KETISH);
        ordersList.forEach(order -> {

                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                    markup.setKeyboard(keyboard);
                    List<InlineKeyboardButton> row = Button.finish(order.getId());
                    keyboard.add(row);
                    sendOrderWithDetail(text, sendMessage, order, markup);
                }
        );

    }


    public void sendOrderWithDetail(String text, SendMessage sendMessage, OrdersEntity order, InlineKeyboardMarkup markup) {

        text += ordersService.getOrderDetail(order);

        text += "\n*Mijoz:* _" + order.getProfile().getFullName() +
                "_\n*Telefon raqam*: _" + order.getProfile().getPhone() + "_";

        sendMessage.setText(text);
        sendMessage.setReplyMarkup(markup);
        myTelegramBot.send(sendMessage);
    }

    public void sendCheckingOrder(SendMessage sendMessage,OrdersEntity order,Long chatId) {

        String message = "Buyurtma holati: Tekshiruvda ❕❗";
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        markup.setKeyboard(keyboard);

        InlineKeyboardButton save = Button.save(order.getProfile().getUserId(), order.getId());
        InlineKeyboardButton cancel = Button.cancel(order.getProfile().getUserId(), order.getId());
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(save);
        row.add(cancel);
        keyboard.add(row);

        if (order.getMethodType().equals(MethodType.OLIB_KETISH)) {
            sendOrderWithDetail(message, sendMessage, order, markup);
            return;
        }

        deliveryService.searchAndDeleteAndRemoveLocationMessageDTO(chatId, order.getId());
        List<InlineKeyboardButton> row2 = Button.locationForAdmin(order.getId());
        keyboard.add(row2);
        sendOrderWithDetail(message, sendMessage, order, markup);
    }
}
