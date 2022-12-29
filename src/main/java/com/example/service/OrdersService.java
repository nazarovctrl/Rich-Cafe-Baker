package com.example.service;

import com.example.admin.service.DeliveryService;
import com.example.controller.MenuController;
import com.example.entity.OrdersEntity;
import com.example.entity.ProfileEntity;
import com.example.enums.MethodType;
import com.example.enums.OrdersStatus;
import com.example.enums.Payment;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.repository.MenuRepository;
import com.example.repository.OrdersRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;


import java.util.Optional;

@Service
public class OrdersService {

    private final OrdersRepository ordersRepository;

    private final MenuRepository menuRepository;
    private final MyTelegramBot myTelegramBot;
    private final MenuController menuController;
    private final OrdersService ordersService;
    private final DeliveryService deliveryService;

    private final AuthService authService;

    @Lazy

    public OrdersService(OrdersRepository ordersRepository, MenuRepository menuRepository, MyTelegramBot myTelegramBot, MenuController menuController, OrdersService ordersService, DeliveryService deliveryService, AuthService authService) {
        this.ordersRepository = ordersRepository;
        this.menuRepository = menuRepository;
        this.myTelegramBot = myTelegramBot;
        this.menuController = menuController;
        this.ordersService = ordersService;
        this.deliveryService = deliveryService;
        this.authService = authService;
    }

    public void deleteByUserId(Long userId) {
        ProfileEntity profile = authService.findByUserId(userId);
        ordersRepository.deleteByProfile_Id(profile.getId());
    }

    public boolean isExists(Message message) {
        return menuRepository.existsByName(message.getText());
    }


    public OrdersEntity add(OrdersEntity entity) {
        Optional<OrdersEntity> optional = ordersRepository.findByProfileAndStatusAndVisibleTrue(entity.getProfile(), OrdersStatus.NOT_CONFIRMED);
        if (optional.isPresent()) {
            return optional.get();
        }

        ordersRepository.save(entity);
        return entity;
    }

    public void changePayment(Long userId, Payment cash) {
        ProfileEntity profile = authService.findByUserId(userId);
        ordersRepository.changePayment(profile.getId(), cash);
    }

    public void changeMethodType(Long userId, MethodType methodType) {
        ProfileEntity profile = authService.findByUserId(userId);
        ordersRepository.changeMethodType(profile.getId(), methodType);
    }

    public void setLocation(Long userId, Double latitude, Double longitude) {
        ProfileEntity profile = authService.findByUserId(userId);
        ordersRepository.setLocation(profile.getId(), latitude, longitude);

    }

    public OrdersEntity getByUserId(Long userId) {

        return ordersRepository.findByProfile_UserIdAndVisibleAndStatus(userId, true, OrdersStatus.NOT_CONFIRMED);
    }

    public void changeStatus(Long userId, OrdersStatus status) {
        ProfileEntity profile = authService.findByUserId(userId);
        ordersRepository.changeStatus(profile.getId(), status);
    }

    public OrdersEntity findById(Integer orderId) {
        Optional<OrdersEntity> optional = ordersRepository.findById(orderId);
        return optional.orElse(null);
    }

    public void changeStatusById(Integer orderId, OrdersStatus status) {
        ordersRepository.changeStatusById(orderId, status);
    }

    public void confirmOrder(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        String[] split = data.split("/");

        String userId = split[1];
        Integer orderId = Integer.valueOf(split[2]);

        OrdersEntity ordersEntity = ordersService.findById(orderId);

        editMessage(update);
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(update.getCallbackQuery().getFrom().getId());
        replyMessage.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());

        if (!ordersEntity.getStatus().equals(OrdersStatus.CHECKING)) {
            replyMessage.setText("Bu buyurtma ko'rib chiqilgan");
            myTelegramBot.send(replyMessage);
            return;
        }


        SendMessage send = new SendMessage();
        send.setChatId(userId);

        if (split[0].equals("save") && ordersEntity.getMethodType().equals(MethodType.YETKAZIB_BERISH)) {
            deliveryService.delivery(ordersEntity, Integer.valueOf(split[3]));
            return;

        }

        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(userId);
        deleteMessage.setMessageId(Integer.valueOf(split[3]));
        myTelegramBot.send(deleteMessage);


        if (split[0].equals("save")) {

            replyMessage.setText("Buyurtma qabul qilindi");
            ordersService.changeStatusById(orderId, OrdersStatus.CONFIRMED);

            send.setText("✅ Buyurtmangiz qabul qilindi " +
                    "\nBuyurtma raqami: " + orderId);


        }

        if (split[0].equals("cancel")) {
            replyMessage.setText("Buyurtma bekor qilindi");
            ordersService.changeStatusById(orderId, OrdersStatus.CANCELLED);
            send.setText("❌ Buyurtmangiz bekor qilindi ");
        }

        myTelegramBot.send(replyMessage); // adminga boradi
        myTelegramBot.send(send); //userga boradi
        menuController.mainMenu(Long.valueOf(userId)); //userri menuga oktizadi

    }

    public void editMessage(Update update) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(update.getCallbackQuery().getFrom().getId());
        editMessageReplyMarkup.setReplyMarkup(null);
        editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        myTelegramBot.send(editMessageReplyMarkup);

    }

    public void update(OrdersEntity orders) {
        ordersRepository.save(orders);
    }
}
