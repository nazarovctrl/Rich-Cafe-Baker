package com.example.admin.service;


import com.example.dto.LocationMessageDTO;
import com.example.entity.AdminEntity;
import com.example.entity.OrdersEntity;
import com.example.enums.OrdersStatus;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.OrdersService;
import com.example.utill.Button;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
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
    private ArrayList<LocationMessageDTO> locationMessageDTOList = new ArrayList<>();

    @Lazy
    public DeliveryService(SupplierService supplierService, MyTelegramBot myTelegramBot, OrdersService ordersService) {
        this.supplierService = supplierService;
        this.myTelegramBot = myTelegramBot;
        this.ordersService = ordersService;
    }


    public void delivery(OrdersEntity ordersEntity) {
        List<AdminEntity> list = supplierService.getEmptySupplierList();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("MARKDOWN");
        sendMessage.setReplyMarkup(Button.deliveryMarkup(ordersEntity.getId()));
        String text = ordersService.getOrderDetail(ordersEntity);
        text += "\n*Mijoz:* _" + ordersEntity.getProfile().getFullName() +
                "_\n*Telefon raqam*: _" + ordersEntity.getProfile().getPhone() + "_";
        sendMessage.setText(text);

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

        OrdersEntity order = ordersService.findById(Integer.valueOf(split[1]));


        if (!order.getStatus().equals(OrdersStatus.CHECKING)) {
            SendMessage replyMessage = new SendMessage();
            replyMessage.setChatId(update.getCallbackQuery().getFrom().getId());
            replyMessage.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());
            replyMessage.setText("Bu buyurtma ko'rib chiqilgan");
            myTelegramBot.send(replyMessage);
            editMessage(update, null);
            return;
        }


        AdminEntity supplier = supplierService.getByUserId(supplierUserId);

        order.setSupplier(supplier);

        ordersService.update(order);


        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();


        List<InlineKeyboardButton> row = Button.finish(order.getId());
        List<InlineKeyboardButton> row2 = Button.location(order.getId(), true);
        keyboard.add(row);
        keyboard.add(row2);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        editMessage(update, markup);

        LocationMessageDTO messageDTO = getLocationMessageDTO(update.getCallbackQuery().getFrom().getId(), order.getId());
        if (messageDTO != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(update.getCallbackQuery().getFrom().getId());
            deleteMessage.setMessageId(messageDTO.getLocationMessageId());
            deleteLocationMessageDTO(messageDTO);
            myTelegramBot.send(deleteMessage);
        }

        ordersService.changeStatusById(order.getId(), OrdersStatus.CONFIRMED);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("MARKDOWN");
        sendMessage.setChatId(order.getProfile().getUserId());
        sendMessage.setText(ordersService.getOrderDetail(order) + " \n\n *✅Buyurtma qabul qilindi✅*");
        myTelegramBot.send(sendMessage); //userga boradi


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

        boolean isSupplier = split[2].equals("s");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        markup.setKeyboard(keyboard);

        OrdersEntity order = ordersService.findById(Integer.valueOf(split[1]));

        Long supplierUserId = update.getCallbackQuery().getFrom().getId();
        LocationMessageDTO messageDTO = getLocationMessageDTO(supplierUserId, order.getId());

        if (isSupplier) {
            List<InlineKeyboardButton> row = Button.finish(order.getId());
            keyboard.add(row);
        }

        List<InlineKeyboardButton> row2 = Button.location(order.getId(), isSupplier);


        if (messageDTO == null) {
            row2.get(0).setText("\uD83D\uDCCD Yetkazib berish manzili (yopish)");
        }
        keyboard.add(row2);

        editMessage(update, markup);

        if (messageDTO != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(supplierUserId);
            deleteMessage.setMessageId(messageDTO.getLocationMessageId());
            locationMessageDTOList.remove(messageDTO);
            myTelegramBot.send(deleteMessage);
            return;
        }

        SendLocation sendLocation = new SendLocation();
        sendLocation.setChatId(update.getCallbackQuery().getFrom().getId());
        sendLocation.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());
        sendLocation.setLongitude(order.getLongitude());
        sendLocation.setLatitude(order.getLatitude());

        Message message = myTelegramBot.send(sendLocation);

        messageDTO = new LocationMessageDTO();
        messageDTO.setOrderId(order.getId());
        messageDTO.setSupplierUserId(update.getCallbackQuery().getFrom().getId());
        messageDTO.setLocationMessageId(message.getMessageId());

        locationMessageDTOList.add(messageDTO);


    }

    public void cancelDeliveryRequest(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        String[] split = data.split("/");
        Integer orderId = Integer.valueOf(split[1]);

        Long supplierUserId = update.getCallbackQuery().getFrom().getId();
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(supplierUserId);
        deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        myTelegramBot.send(deleteMessage);


        locationMessageDTOList.forEach(dto -> {
            if (dto.getSupplierUserId().equals(supplierUserId) && dto.getOrderId().equals(orderId)) {
                deleteMessage.setMessageId(dto.getLocationMessageId());
                myTelegramBot.send(deleteMessage);
            }
        });


    }

    public void getLocationForAdmin(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        String[] split = data.split("/");

        OrdersEntity order = ordersService.findById(Integer.valueOf(split[1]));


        Long supplierUserId = update.getCallbackQuery().getFrom().getId();
        LocationMessageDTO messageDTO = getLocationMessageDTO(supplierUserId, order.getId());

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        markup.setKeyboard(keyboard);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(Button.save(order.getProfile().getUserId(), order.getId()));
        row.add(Button.cancel(order.getProfile().getUserId(), order.getId()));

        List<InlineKeyboardButton> row2 = Button.locationForAdmin(order.getId());
        if (messageDTO == null) {
            row2.get(0).setText("\uD83D\uDCCD Yetkazib berish manzili (yopish)");
        }
        keyboard.add(row);
        keyboard.add(row2);
        try {
            editMessage(update, markup);
        } catch (Exception e) {
            markup = new InlineKeyboardMarkup();
            keyboard = new ArrayList<>();
            markup.setKeyboard(keyboard);
            row = new ArrayList<>();
            row.add(Button.save(order.getProfile().getUserId(), order.getId()));
            row.add(Button.cancel(order.getProfile().getUserId(), order.getId()));
            row2 = Button.locationForAdmin(order.getId());
            keyboard.add(row);
            keyboard.add(row2);
            try {
                editMessage(update, markup);
            } catch (Exception ex) {
                markup = new InlineKeyboardMarkup();
                keyboard = new ArrayList<>();
                markup.setKeyboard(keyboard);
                row = new ArrayList<>();
                row.add(Button.save(order.getProfile().getUserId(), order.getId()));
                row.add(Button.cancel(order.getProfile().getUserId(), order.getId()));
                row2 = Button.locationForAdmin(order.getId());
                row2.get(0).setText("\uD83D\uDCCD Yetkazib berish manzili (yopish)");
                keyboard.add(row);
                keyboard.add(row2);
            }
        }


        if (messageDTO != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(supplierUserId);
            deleteMessage.setMessageId(messageDTO.getLocationMessageId());
            locationMessageDTOList.remove(messageDTO);
            myTelegramBot.send(deleteMessage);
            return;
        }


        SendLocation sendLocation = new SendLocation();
        sendLocation.setChatId(update.getCallbackQuery().getFrom().getId());
        sendLocation.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());
        sendLocation.setLongitude(order.getLongitude());
        sendLocation.setLatitude(order.getLatitude());

        Message location = myTelegramBot.send(sendLocation);

        messageDTO = new LocationMessageDTO();
        messageDTO.setOrderId(order.getId());
        messageDTO.setSupplierUserId(update.getCallbackQuery().getFrom().getId());
        messageDTO.setLocationMessageId(location.getMessageId());
        locationMessageDTOList.add(messageDTO);
    }

    public void getLocationForSupplier(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        String[] split = data.split("/");

        OrdersEntity order = ordersService.findById(Integer.valueOf(split[1]));


        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        markup.setKeyboard(keyboard);

        Long supplierUserId = update.getCallbackQuery().getFrom().getId();

        LocationMessageDTO messageDTO = getLocationMessageDTO(supplierUserId, order.getId());

        List<InlineKeyboardButton> row = Button.delivery(order.getId());
        row.add(Button.cancelForSupplier(order.getId()));

        List<InlineKeyboardButton> row2 = Button.locationForSupplier(order.getId());
        if (messageDTO == null) {
            row2.get(0).setText("\uD83D\uDCCD Yetkazib berish manzili (yopish)");
        }
        keyboard.add(row);
        keyboard.add(row2);
        editMessage(update, markup);


        if (messageDTO != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(supplierUserId);
            deleteMessage.setMessageId(messageDTO.getLocationMessageId());
            locationMessageDTOList.remove(messageDTO);
            myTelegramBot.send(deleteMessage);
            return;
        }


        SendLocation sendLocation = new SendLocation();
        sendLocation.setChatId(update.getCallbackQuery().getFrom().getId());
        sendLocation.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());
        sendLocation.setLongitude(order.getLongitude());
        sendLocation.setLatitude(order.getLatitude());

        Message location = myTelegramBot.send(sendLocation);

        messageDTO = new LocationMessageDTO();
        messageDTO.setOrderId(order.getId());
        messageDTO.setSupplierUserId(update.getCallbackQuery().getFrom().getId());
        messageDTO.setLocationMessageId(location.getMessageId());
        locationMessageDTOList.add(messageDTO);
    }

    public LocationMessageDTO getLocationMessageDTO(Long userId, Integer orderId) {

        return locationMessageDTOList.stream().filter(dto ->
                dto.getSupplierUserId().equals(userId) &&
                        dto.getOrderId().equals(orderId)).findAny().orElse(null);
    }

    public void deleteLocationMessageDTO(LocationMessageDTO messageDTO) {
        locationMessageDTOList.remove(messageDTO);
    }

    public void searchAndDeleteAndRemoveLocationMessageDTO(Long userId, Integer orderId) {

        LocationMessageDTO messageDTO = locationMessageDTOList.stream().filter(dto ->
                dto.getSupplierUserId().equals(userId) &&
                        dto.getOrderId().equals(orderId)).findAny().orElse(null);

        if (messageDTO == null) {
            return;
        }
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(userId);
        deleteMessage.setMessageId(messageDTO.getLocationMessageId());
        locationMessageDTOList.remove(messageDTO);
        myTelegramBot.send(deleteMessage);


    }
}