package com.example.admin.service;

import com.example.controller.MenuController;
import com.example.entity.AdminEntity;
import com.example.entity.OrdersEntity;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.OrdersService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
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

    @Lazy
    public DeliveryService(SupplierService supplierService, MyTelegramBot myTelegramBot, OrdersService ordersService, MenuController menuController) {
        this.supplierService = supplierService;
        this.myTelegramBot = myTelegramBot;
        this.ordersService = ordersService;
        this.menuController = menuController;
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

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("*Buyurtma raqami:* " + ordersEntity.getId());

        sendMessage.setParseMode("MARKDOWN");
        sendMessage.setReplyMarkup(markup);

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

        AdminEntity supplier = supplierService.getByUserId(supplierUserId);

        orders.setSupplier(supplier);

        ordersService.update(orders);

        editMessage(update);

        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(update.getCallbackQuery().getFrom().getId());
        replyMessage.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());
        replyMessage.setText("Qabul qilindi");
        myTelegramBot.send(replyMessage);

        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(orders.getProfile().getUserId());
        deleteMessage.setMessageId(Integer.valueOf(split[2]));
        myTelegramBot.send(deleteMessage);
        menuController.mainMenu(orders.getProfile().getUserId()); //userri menuga oktizadi

    }

    public void editMessage(Update update) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(update.getCallbackQuery().getFrom().getId());
        editMessageReplyMarkup.setReplyMarkup(null);
        editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        myTelegramBot.send(editMessageReplyMarkup);

    }
}