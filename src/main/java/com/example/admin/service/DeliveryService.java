package com.example.admin.service;

import com.example.controller.MenuController;
import com.example.entity.AdminEntity;
import com.example.entity.OrderMealEntity;
import com.example.entity.OrdersEntity;
import com.example.enums.OrdersStatus;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.OrderMealService;
import com.example.service.OrdersService;
import com.example.utill.Button;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeliveryService {
    private final SupplierService supplierService;
    private final MyTelegramBot myTelegramBot;
    private final OrdersService ordersService;
    private final MenuController menuController;
    private final OrderMealService orderMealService;


    @Lazy
    public DeliveryService(SupplierService supplierService, MyTelegramBot myTelegramBot, OrdersService ordersService, MenuController menuController, OrderMealService orderMealService) {
        this.supplierService = supplierService;
        this.myTelegramBot = myTelegramBot;
        this.ordersService = ordersService;
        this.menuController = menuController;
        this.orderMealService = orderMealService;
    }

    public void delivery(OrdersEntity ordersEntity, Integer messageId) {
        List<AdminEntity> list = supplierService.getEmptySupplierList();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(" Qabul qilish");
        button.setCallbackData("delivery/" + ordersEntity.getId() + "/" + messageId);


        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);

        List<InlineKeyboardButton> row2 = Button.location(ordersEntity.getId());
        keyboard.add(row2);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("MARKDOWN");
        sendMessage.setReplyMarkup(markup);

        List<OrderMealEntity> oderMealList = orderMealService.getListByOrderId(ordersEntity.getId());

        StringBuilder text = new StringBuilder("\uD83D\uDCE5 *Buyurtma :* \n\n");

        text.append("*Buyurtma raqami: ").append(ordersEntity.getId()).append("* \n");
        double total = 0;
        for (OrderMealEntity entity : oderMealList) {
            text.append(entity.getMeal().getName()).append("\n").append(entity.getQuantity()).append(" x ").append(entity.getMeal().getPrice()).append(" = ").append(entity.getMeal().getPrice() * entity.getQuantity()).append(" so'm \n\n");
            total += entity.getMeal().getPrice() * entity.getQuantity();
        }
        text.append("\n Jami: ").append(total).append(" so'm");

        sendMessage.setText(text.toString());

        for (AdminEntity adminEntity : list) {
            sendMessage.setChatId(adminEntity.getUserId());
            myTelegramBot.send(sendMessage);
        }

    }

    public void confirmDelivery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        String[] split = data.split("/");


        Long supplierUserId = update.getCallbackQuery().getFrom().getId();

        OrdersEntity orders = ordersService.findById(Integer.valueOf(split[1]));


        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(update.getCallbackQuery().getFrom().getId());
        replyMessage.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());

        if (!orders.getStatus().equals(OrdersStatus.CHECKING)) {
            replyMessage.setText("Bu buyurtma ko'rib chiqilgan");
            editMessage(update, null);
            myTelegramBot.send(replyMessage);
            return;
        }

        AdminEntity supplier = supplierService.getByUserId(supplierUserId);

        orders.setSupplier(supplier);

        ordersService.update(orders);

        List<InlineKeyboardButton> row = Button.location(orders.getId());
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        editMessage(update, markup);

        ordersService.changeStatusById(orders.getId(), OrdersStatus.CONFIRMED);


        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(orders.getProfile().getUserId());
        deleteMessage.setMessageId(Integer.valueOf(split[2]));
        myTelegramBot.send(deleteMessage);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("✅ Buyurtmangiz qabul qilindi \n Buyurtma raqami: " + orders.getId());
        sendMessage.setChatId(orders.getProfile().getUserId());
        myTelegramBot.send(sendMessage);  // userga order haqida ma'lumot

        menuController.mainMenu(orders.getProfile().getUserId()); //userri menuga oktizadi

    }

    public void editMessage(Update update, InlineKeyboardMarkup markup) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(update.getCallbackQuery().getFrom().getId());
        editMessageReplyMarkup.setReplyMarkup(markup);
        editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        myTelegramBot.send(editMessageReplyMarkup);

    }

    public void getLocation(Update update) {

        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        String[] split = data.split("/");

        OrdersEntity orders = ordersService.findById(Integer.valueOf(split[1]));

        SendLocation sendLocation = new SendLocation();
        sendLocation.setChatId(update.getCallbackQuery().getFrom().getId());
        sendLocation.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());

        sendLocation.setLongitude(orders.getLongitude());
        sendLocation.setLatitude(orders.getLatitude());

        myTelegramBot.send(sendLocation);
    }
}