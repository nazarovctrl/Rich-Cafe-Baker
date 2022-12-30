package com.example.admin.service;

import com.example.controller.MenuController;
import com.example.dto.LocationMessageDTO;
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
    private final MenuController menuController;
    private final OrderMealService orderMealService;

    private ArrayList<LocationMessageDTO> locationMessageDTOList = new ArrayList<>();

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


        List<InlineKeyboardButton> row = Button.delivery(ordersEntity.getId(), messageId);


        row.add(Button.cancelForSupplier(ordersEntity.getId()));

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);

        List<InlineKeyboardButton> row2 = Button.locationForSupplier(ordersEntity.getId(), messageId);
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

        LocationMessageDTO messageDTO = getLocationMessageDTO(update, order.getId());
        if (messageDTO != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(update.getCallbackQuery().getFrom().getId());
            deleteMessage.setMessageId(messageDTO.getMessageId());
            deleteLocationMessageDTO(messageDTO);
            myTelegramBot.send(deleteMessage);
        }

        ordersService.changeStatusById(order.getId(), OrdersStatus.CONFIRMED);


        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(order.getProfile().getUserId());
        deleteMessage.setMessageId(Integer.valueOf(split[2]));
        myTelegramBot.send(deleteMessage);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("✅ Buyurtmangiz qabul qilindi \n Buyurtma raqami: " + order.getId());
        sendMessage.setChatId(order.getProfile().getUserId());
        myTelegramBot.send(sendMessage);  // userga order haqida ma'lumot

        menuController.mainMenu(order.getProfile().getUserId()); //userri menuga oktizadi

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
        boolean isSupplier = false;
        if (split[2].equals("s")) {
            isSupplier = true;
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        markup.setKeyboard(keyboard);

        OrdersEntity order = ordersService.findById(Integer.valueOf(split[1]));

        Long supplierUserId = update.getCallbackQuery().getFrom().getId();
        LocationMessageDTO messageDTO = locationMessageDTOList.stream().filter(dto -> dto.getSupplierUserId().equals(supplierUserId) && dto.getOrderId().equals(order.getId())).findAny().orElse(null);

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
            deleteMessage.setMessageId(messageDTO.getMessageId());
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
        messageDTO.setMessageId(message.getMessageId());
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
                        deleteMessage.setMessageId(dto.getMessageId());
                        myTelegramBot.send(deleteMessage);
                    }
                }
        );


    }

    public void getLocationForAdmin(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        String[] split = data.split("/");

        OrdersEntity order = ordersService.findById(Integer.valueOf(split[1]));


        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        markup.setKeyboard(keyboard);

        Integer messageId = Integer.valueOf(split[2]);


        Long supplierUserId = update.getCallbackQuery().getFrom().getId();
        LocationMessageDTO messageDTO = locationMessageDTOList.stream().filter(dto -> dto.getSupplierUserId().equals(supplierUserId) && dto.getOrderId().equals(order.getId())).findAny().orElse(null);


        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(Button.save(order.getProfile().getUserId(), order.getId(), messageId));
        row.add(Button.cancel(order.getProfile().getUserId(), order.getId(), messageId));

        List<InlineKeyboardButton> row2 = Button.locationForAdmin(order.getId(), messageId);
        if (messageDTO == null) {
            row2.get(0).setText("\uD83D\uDCCD Yetkazib berish manzili (yopish)");
        }
        keyboard.add(row);
        keyboard.add(row2);
        editMessage(update, markup);


        if (messageDTO != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(supplierUserId);
            deleteMessage.setMessageId(messageDTO.getMessageId());
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
        messageDTO.setMessageId(message.getMessageId());
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

        Integer messageId = Integer.valueOf(split[2]);


        Long supplierUserId = update.getCallbackQuery().getFrom().getId();
        LocationMessageDTO messageDTO = locationMessageDTOList.stream().filter(dto -> dto.getSupplierUserId().equals(supplierUserId) && dto.getOrderId().equals(order.getId())).findAny().orElse(null);


        List<InlineKeyboardButton> row = Button.delivery(order.getId(), messageId);
        row.add(Button.cancelForSupplier(order.getId()));

        List<InlineKeyboardButton> row2 = Button.locationForSupplier(order.getId(), messageId);
        if (messageDTO == null) {
            row2.get(0).setText("\uD83D\uDCCD Yetkazib berish manzili (yopish)");
        }
        keyboard.add(row);
        keyboard.add(row2);
        editMessage(update, markup);


        if (messageDTO != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(supplierUserId);
            deleteMessage.setMessageId(messageDTO.getMessageId());
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
        messageDTO.setMessageId(message.getMessageId());
        locationMessageDTOList.add(messageDTO);
    }

    public LocationMessageDTO getLocationMessageDTO(Update update, Integer orderId) {

        Long supplierUserId = update.getCallbackQuery().getFrom().getId();
        return locationMessageDTOList.stream().filter(dto -> dto.getSupplierUserId().equals(supplierUserId) && dto.getOrderId().equals(orderId)).findAny().orElse(null);
    }

    public void deleteLocationMessageDTO(LocationMessageDTO messageDTO) {
        boolean remove = locationMessageDTOList.remove(messageDTO);
    }
}