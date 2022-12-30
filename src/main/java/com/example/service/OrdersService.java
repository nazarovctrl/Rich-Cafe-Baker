package com.example.service;

import com.example.admin.service.DeliveryService;
import com.example.controller.MenuController;
import com.example.dto.LocationMessageDTO;
import com.example.entity.OrderMealEntity;
import com.example.entity.OrdersEntity;
import com.example.entity.ProfileEntity;
import com.example.enums.MethodType;
import com.example.enums.OrdersStatus;
import com.example.enums.Payment;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.repository.MenuRepository;
import com.example.repository.OrdersRepository;
import com.example.utill.Button;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    private final OrderMealService orderMealService;

    @Lazy
    public OrdersService(OrdersRepository ordersRepository, MenuRepository menuRepository, MyTelegramBot myTelegramBot, MenuController menuController, OrdersService ordersService, DeliveryService deliveryService, AuthService authService, OrderMealService orderMealService) {
        this.ordersRepository = ordersRepository;
        this.menuRepository = menuRepository;
        this.myTelegramBot = myTelegramBot;
        this.menuController = menuController;
        this.ordersService = ordersService;
        this.deliveryService = deliveryService;
        this.authService = authService;
        this.orderMealService = orderMealService;
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

    public void editMessage(Update update, InlineKeyboardMarkup markup) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(update.getCallbackQuery().getFrom().getId());
        editMessageReplyMarkup.setReplyMarkup(markup);
        editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        myTelegramBot.send(editMessageReplyMarkup);

    }

    public void update(OrdersEntity orders) {
        ordersRepository.save(orders);
    }

    public void getOrdersHistory(Long chatId) {
        List<OrdersEntity> entityList = ordersRepository.getOrdersHistoryListByUserId(chatId, OrdersStatus.FINISHED);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("MARKDOWN");

        for (OrdersEntity ordersEntity : entityList) {
            sendMessage.setText(getOrderDetail(ordersEntity));
            myTelegramBot.send(sendMessage);
        }

    }

    public String getOrderDetail(OrdersEntity ordersEntity) {
        StringBuilder text = new StringBuilder();
        LocalDateTime createdDate = ordersEntity.getCreatedDate();
        text.append("*Buyurtma raqami: ").append(ordersEntity.getId());
        text.append("\nSana: ").append(createdDate.toLocalDate());
        text.append("\nVaqt: ").append(createdDate.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        Payment payment = ordersEntity.getPayment();
        String kind = "";
        if (payment.equals(Payment.CASH)) {
            kind = Constant.cash;
        }
        text.append("\nTo'lov turi: ").append(kind);

        MethodType methodType = ordersEntity.getMethodType();
        String type = "";
        if (methodType.equals(MethodType.OLIB_KETISH)) {
            type = "Olib ketish";
        } else {
            type = "\uD83D\uDEF5 Yetkazib berish ";
            if (ordersEntity.getSupplier() != null) {
                type += "\nYetkazib beruvchi: " + ordersEntity.getSupplier().getFullname() +
                        "\nTelefon raqam: " + ordersEntity.getSupplier().getPhone();
            }
        }
        text.append("\nBuyurtma turi: ").append(type).append("*\n\n");


        List<OrderMealEntity> mealList = orderMealService.getListByOrderId(ordersEntity.getId());
        double total = 0;
        for (OrderMealEntity entity : mealList) {
            text.append(entity.getMeal().getName()).append("\n").append(entity.getQuantity()).append(" x ")
                    .append(entity.getMeal().getPrice()).append(" = ")
                    .append(entity.getMeal().getPrice() * entity.getQuantity()).append(" so'm \n");
            total += entity.getMeal().getPrice() * entity.getQuantity();
        }
        text.append("\nJami: ").append(total).append(" so'm\n");
        return text.toString();
    }

    public boolean check(Update update, Integer orderId) {
        OrdersEntity ordersEntity = ordersService.findById(orderId);

        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(update.getCallbackQuery().getFrom().getId());
        replyMessage.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());

        if (!ordersEntity.getStatus().equals(OrdersStatus.CHECKING)) {
            replyMessage.setText("Bu buyurtma ko'rib chiqilgan");
            myTelegramBot.send(replyMessage);
            editMessage(update, null);
            return false;
        }

        return true;
    }

    public void save(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        String[] split = data.split("/");

        String userId = split[1];
        Integer orderId = Integer.valueOf(split[2]);

        OrdersEntity order = ordersService.findById(orderId);


        if (!check(update, orderId)) {
            return;
        }


        if (order.getMethodType().equals(MethodType.YETKAZIB_BERISH)) {

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            markup.setKeyboard(keyboard);

            LocationMessageDTO messageDTO = deliveryService.getLocationMessageDTO(update.getCallbackQuery().getFrom().getId(), orderId);
            if (messageDTO != null) {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(update.getCallbackQuery().getFrom().getId());
                deleteMessage.setMessageId(messageDTO.getLocationMessageId());
                deliveryService.deleteLocationMessageDTO(messageDTO);
                myTelegramBot.send(deleteMessage);
            }


            List<InlineKeyboardButton> row = Button.location(orderId, false);
            keyboard.add(row);
            editMessage(update, markup);

            deliveryService.delivery(order);
            return;
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        markup.setKeyboard(keyboard);

        List<InlineKeyboardButton> row = Button.finish(orderId);
        keyboard.add(row);

        editMessage(update, markup);

        ordersService.changeStatusById(orderId, OrdersStatus.CONFIRMED);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("MARKDOWN");
        sendMessage.setChatId(order.getProfile().getUserId());
        sendMessage.setText(getOrderDetail(order) + " \n\n *✅Buyurtma qabul qilindi✅*");
        myTelegramBot.send(sendMessage); //userga boradi
        menuController.mainMenu(Long.valueOf(userId)); //userri menuga oktizadi


    }

    public void cancel(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        String[] split = data.split("/");

        String userId = split[1];
        Integer orderId = Integer.valueOf(split[2]);
        OrdersEntity order = ordersService.findById(orderId);

        if (!check(update, orderId)) {
            return;
        }

        editMessage(update, null);

        ordersService.changeStatusById(orderId, OrdersStatus.CANCELLED);


        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(update.getCallbackQuery().getFrom().getId());
        replyMessage.setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId());
        replyMessage.setText("❌ Buyurtma bekor qilindi");
        myTelegramBot.send(replyMessage); // adminga boradi


        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("MARKDOWN");
        sendMessage.setChatId(userId);
        sendMessage.setText(getOrderDetail(order) + " \n\n* ❌ Buyurtma bekor qilindi ❌*");
        myTelegramBot.send(sendMessage);

        LocationMessageDTO messageDTO = deliveryService.getLocationMessageDTO(update.getCallbackQuery().getFrom().getId(), orderId);
        if (messageDTO != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(messageDTO.getSupplierUserId());
            deleteMessage.setMessageId(messageDTO.getLocationMessageId());
            myTelegramBot.send(deleteMessage);
        }

    }

    public void finish(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        String[] split = data.split("/");
        Integer orderId = Integer.valueOf(split[1]);
        changeStatusById(orderId, OrdersStatus.FINISHED);

        editMessage(update, null);

        OrdersEntity order = ordersService.findById(orderId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("MARKDOWN");
        sendMessage.setChatId(order.getProfile().getUserId());
        sendMessage.setText(getOrderDetail(order) + " \n\n *❇️Buyurtma yakunlandi!❇️*");

        LocationMessageDTO messageDTO = deliveryService.getLocationMessageDTO(update.getCallbackQuery().getFrom().getId(), orderId);
        if (messageDTO != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(messageDTO.getSupplierUserId());
            deleteMessage.setMessageId(messageDTO.getLocationMessageId());
            myTelegramBot.send(deleteMessage);
        }
        myTelegramBot.send(sendMessage);

    }

    public List<OrdersEntity> getListBySupplierUserId(Long userId, OrdersStatus status) {
        return ordersRepository.findBySupplierUserId(userId, status);
    }


    public List<OrdersEntity> getListByStatusAndMethodType(OrdersStatus status, MethodType type) {
        return ordersRepository.findByStatusAndMethodType(status, type);
    }
}
