package com.example.controller;

import com.example.admin.service.SettingsService;
import com.example.entity.AdminEntity;
import com.example.entity.OrderMealEntity;

import com.example.entity.OrdersEntity;
import com.example.entity.ProfileEntity;
import com.example.enums.MethodType;
import com.example.enums.OrdersStatus;
import com.example.enums.Payment;
import com.example.enums.Step;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.AuthService;
import com.example.service.OrderMealService;
import com.example.service.OrdersService;
import com.example.step.TelegramUsers;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.model.GeocoderRequest;
import org.apache.poi.ss.formula.functions.Address;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class FormalizationController {

    private final MyTelegramBot myTelegramBot;

    private final AuthService authService;

    private final SettingsService settingsService;
    private final MenuController menuController;

    private final OrdersService ordersService;

    private final OrderMealService orderMealService;
    private final MainController mainController;
    private final OrdersController ordersController;

    private List<TelegramUsers> usersList = new ArrayList<>();

    @Lazy
    public FormalizationController(MyTelegramBot myTelegramBot, AuthService authService, SettingsService settingsService, MenuController menuController, OrdersService ordersService, OrderMealService orderMealService, MainController mainController, OrdersController ordersController) {
        this.myTelegramBot = myTelegramBot;
        this.authService = authService;
        this.settingsService = settingsService;
        this.menuController = menuController;
        this.ordersService = ordersService;
        this.orderMealService = orderMealService;
        this.mainController = mainController;
        this.ordersController = ordersController;
    }

    public void handle(Message message) {

        TelegramUsers step = saveUser(message.getChatId());

        if (message.hasText()) {

            String text = message.getText();


            switch (text) {

                case Constant.tozalash -> {
                    ordersService.deleteByUserId(message.getChatId());
                    myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                            "Savatchangiz tozalandi"));
                    step.setStep(null);
                    TelegramUsers saveUser = mainController.saveUser(message.getChatId());
                    saveUser.setStep(Step.AddOrder);
                    TelegramUsers saveUser1 = ordersController.saveUser(message.getChatId());
                    saveUser1.setStep(null);

                    menuController.orderMenu(message);

                    return;
                }

                case Constant.back -> {
                    if (step.getStep() == null || step.getStep().equals(Step.SAVAT)) {
                        menuController.orderMenu(message);
                        TelegramUsers saveUser = ordersController.saveUser(message.getChatId());
                        saveUser.setStep(null);
                        step.setStep(null);
                        return;
                    }
                    if (step.getStep().equals(Step.TYPE_METHOD)) {
                        typePayment(message);
                        step.setStep(Step.CASH);
                        return;
                    }
                    if (step.getStep().equals(Step.CASH)) {
                        boolean b = ordersList(message);
                        if (b) step.setStep(Step.SAVAT);
                        return;
                    }

                    if (step.getStep().equals(Step.OLIB_KETISH) ||
                            step.getStep().equals(Step.DELIVERY)) {
                        typeMethod(message);
                        step.setStep(Step.TYPE_METHOD);
                        return;
                    }

                    if (step.getStep().equals(Step.CONFIRM)) {
                        delivery(message);
                        step.setStep(Step.DELIVERY);
                        return;
                    }

                    if (step.getStep().equals(Step.MAIN)) {
                        TelegramUsers telegramUsers = mainController.saveUser(message.getChatId());
                        telegramUsers.setStep(Step.MAIN);
                        menuController.mainMenu(message);
                        return;
                    }

                }

                case Constant.cash -> {
                    typeMethod(message);
                    step.setStep(Step.TYPE_METHOD);
                    ordersService.changePayment(message.getChatId(), Payment.CASH);
                    return;
                }
                case Constant.savat -> {
                    boolean b = ordersList(message);
                    if (b) step.setStep(Step.SAVAT);
                    return;
                }
                case Constant.home -> {
                    TelegramUsers mainStep = mainController.saveUser(message.getChatId());
                    mainStep.setStep(Step.MAIN);
                    menuController.mainMenu(message);
                    step.setStep(null);
                    return;
                }

            }


            if (message.hasText()) {
                String text1 = message.getText();
                switch (text1) {
                    case Constant.yetkazish -> {
                        ordersService.changeMethodType(message.getChatId(), MethodType.YETKAZIB_BERISH);
                        delivery(message);
                        step.setStep(Step.DELIVERY);
                        return;
                    }
                    case Constant.olibKetish -> {
                        ordersService.changeMethodType(message.getChatId(), MethodType.OLIB_KETISH);
                        olibKetish(message);
                        step.setStep(Step.OLIB_KETISH);
                        return;
                    }
                    case Constant.formalization -> {
                        typePayment(message);
                        step.setStep(Step.CASH);
                        return;
                    }
                    case Constant.confirm -> {
                        checkOrder(message);
                        step.setStep(null);
                        TelegramUsers saveUser = mainController.saveUser(message.getChatId());
                        saveUser.setStep(null);
                        TelegramUsers saveUser1 = ordersController.saveUser(message.getChatId());
                        saveUser1.setStep(null);
                        return;
                    }
                    case Constant.cancel -> {
                        ordersService.deleteByUserId(message.getChatId());
                        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                                "Savatchangiz tozalandi"));
                        step.setStep(null);
                        TelegramUsers saveUser = mainController.saveUser(message.getChatId());
                        saveUser.setStep(Step.AddOrder);
                        TelegramUsers saveUser1 = ordersController.saveUser(message.getChatId());
                        saveUser1.setStep(null);

                        menuController.orderMenu(message);

                        return;
                    }
                }


            }


        }


    }

    private void checkOrder(Message message) {
        /// TODO send to admin for checking orders
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);

        SendMessage sendMessage1 = new SendMessage();
        sendMessage1.setText("⌛️");
        sendMessage1.setChatId(message.getChatId());
        sendMessage1.setReplyMarkup(replyKeyboardRemove);
        Message send = myTelegramBot.send(sendMessage1);


        List<AdminEntity> adminList = settingsService.getAdminList();

        SendMessage sendMessage = new SendMessage();

        sendMessage.setParseMode("MARKDOWN");

        List<OrderMealEntity> oderMealList = orderMealService.getNOTCONFIRMEDOderMealList(message.getChatId());
        OrdersEntity orders = ordersService.getByUserId(message.getChatId());
        ordersService.changeStatus(message.getChatId(), OrdersStatus.CHECKING);

        ProfileEntity mijoz = authService.findByUserId(message.getChatId());

        StringBuilder text = new StringBuilder("\uD83D\uDCE5 *Buyurtma :* \n\n");

        text.append("*Buyurtma raqami: ").append(orders.getId()).append("* \n");
        for (OrderMealEntity entity : oderMealList) {
            text.append(entity.getMeal().getName()).append("\n").append(entity.getQuantity()).append(" x ").append(entity.getMeal().getPrice()).append(" = ").append(entity.getMeal().getPrice() * entity.getQuantity()).append("\n\n");

        }
        text.append("*Buyurtma turi:* ");

        if (orders.getMethodType().equals(MethodType.OLIB_KETISH)) {
            text.append("_Olib ketish_");
        } else {
            text.append("\uD83D\uDEF5 _Yetkazib berish_");
        }

        text.append("\n *To'lov turi:* ");

        if (orders.getPayment() == Payment.CASH) {
            text.append("\uD83D\uDCB5 _Naqd_");
        }
        text.append("\n Mijoz: ").append(mijoz.getFullName());
        text.append("\n Telefon raqam : ").append(mijoz.getPhone());

        sendMessage.setText(text.toString());


        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("✅ Qabul qilish");
        button.setCallbackData("save/" + mijoz.getUserId() + "/" + orders.getId() + "/" + send.getMessageId());

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("❌ Bekor qilish");
        button2.setCallbackData("cancel/" + mijoz.getUserId() + "/" + orders.getId() + "/" + send.getMessageId());

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        row.add(button2);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(markup);

        for (AdminEntity admin : adminList) {
            sendMessage.setChatId(admin.getUserId());
            myTelegramBot.send(sendMessage);
        }


    }

    private void olibKetish(Message message) {

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(message.getChatId());
        InputFile inputFile = new InputFile();
        File file = new File("attach/address.png");
        inputFile.setMedia(file);

        sendPhoto.setPhoto(inputFile);


        String str = "\uD83D\uDCCD Olib ketish uchun manzil : ";

        str += "\n Qarshi shahri, 3-mikrorayon, Yubileyniy tabaka orqa tomoni";
        str += "\n <a href=\"https://www.google.com/maps/place/Rich+bakery+uz/@38.8433167,65.8029988,17z/data=!3m1!4b1!4m5!3m4!1s0x3f4ea902e3ececbb:0x6e7ba51076d43d34!8m2!3d38.8433125!4d65.8051875\">xaritadan ko'rish</a>";


        sendPhoto.setCaption(str);
        sendPhoto.setParseMode("HTML");

        myTelegramBot.send(sendPhoto);
        confirm(message);

    }


    private void confirm(Message message) {
        TelegramUsers step = saveUser(message.getChatId());
        List<OrderMealEntity> oderMealList = orderMealService.getNOTCONFIRMEDOderMealList(message.getChatId());
        OrdersEntity orders = ordersService.getByUserId(message.getChatId());

        if (oderMealList.isEmpty() || orders == null) {
            System.out.println("order");
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Sizning savatingiz bo'sh \n, buyurtma berish uchun mahsulot tanlang"));

            step.setStep(null);
            TelegramUsers saveUser = mainController.saveUser(message.getChatId());
            saveUser.setStep(Step.AddOrder);
            TelegramUsers saveUser1 = ordersController.saveUser(message.getChatId());
            saveUser1.setStep(null);
            menuController.orderMenu(message);

            return;
        }

        if (orders.getPayment() == null) {
            typePayment(message);
            step.setStep(Step.CASH);
            return;
        }
        if (orders.getMethodType() == null) {
            typeMethod(message);
            step.setStep(Step.TYPE_METHOD);
            return;
        }


        String savat = "\uD83D\uDCE5 *Buyurtma :* \n\n";

        for (OrderMealEntity entity : oderMealList) {

            savat += entity.getMeal().getName() + "\n" +
                    entity.getQuantity() + " x " + entity.getMeal().getPrice() + " = " +
                    entity.getMeal().getPrice() * entity.getQuantity() + "\n\n";


        }
        savat += "*Buyurtma turi:* ";

        if (orders.getMethodType().equals(MethodType.OLIB_KETISH)) {
            savat += "_Olib ketish_";
        } else {
            savat += "\uD83D\uDEF5 _Yetkazib berish_";
        }

        savat += "\n *To'lov turi:* ";

        if (orders.getPayment() == Payment.CASH) {
            savat += "\uD83D\uDCB5 _Naqd_";
        }


        myTelegramBot.send(
                SendMsg.sendMsgParse(
                        message.getChatId(),
                        savat,
                        Button.markup(
                                Button.rowList(
                                        Button.row(Button.button(Constant.confirm)),
                                        Button.row(Button.button(Constant.back), Button.button(Constant.cancel))
                                )
                        )
                )
        );


    }

    private void delivery(Message message) {

        String str = "";

        myTelegramBot.send(
                SendMsg.sendMsg(
                        message.getChatId(),
                        "Yetkazib berish manzilini jonating",
                        Button.markup(
                                Button.rowList(
                                        Button.row(Button.location()),
                                        Button.row(Button.button(Constant.back))
                                )

                        )

                )
        );

    }

    private void deleteAllOrderByUserId(Long chatId) {
        ordersService.deleteByUserId(chatId);
    }


    public void typePayment(Message message) {
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "To`lov turini yuboring",
                Button.markup(Button.rowList(
                        Button.row(Button.button(Constant.cash)),
                        Button.row(Button.button(Constant.back))
                ))));
    }

    public void typeMethod(Message message) {
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Yetkazib berish turini tanlang",
                Button.markup(Button.rowList(
                        Button.row(Button.button(Constant.olibKetish),
                                Button.button(Constant.yetkazish)),
                        Button.row(Button.button(Constant.back))
                ))));
    }

    public boolean ordersList(Message message) {
        TelegramUsers step = saveUser(message.getChatId());

        List<OrderMealEntity> oderMealList = orderMealService.getNOTCONFIRMEDOderMealList(message.getChatId());

        if (oderMealList.isEmpty()) {
            System.out.println("order");
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Sizning savatingiz bo'sh, buyurtma berish uchun mahsulot tanlang"));

            step.setStep(null);
            TelegramUsers saveUser = mainController.saveUser(message.getChatId());
            saveUser.setStep(Step.AddOrder);


            TelegramUsers order = ordersController.saveUser(message.getChatId());
            order.setStep(Step.MAIN);

            menuController.orderMenu(message);

            return false;
        }

        String savat = "\uD83D\uDCE5 Savat: \n\n";

        for (OrderMealEntity entity : oderMealList) {

            savat += entity.getMeal().getName() + "\n" +
                    entity.getQuantity() + " x " + entity.getMeal().getPrice() + " = " +
                    entity.getMeal().getPrice() * entity.getQuantity() + "\n\n";


        }

        myTelegramBot.send(
                SendMsg.sendMsg(
                        message.getChatId(),
                        savat,
                        Button.markup(
                                Button.rowList(
                                        Button.row(Button.button(Constant.formalization)),
                                        Button.row(Button.button(Constant.back), Button.button(Constant.tozalash))
                                )
                        )
                )
        );

        return true;
//        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
//                "\uD83D\uDCE5 Savat: \n\n" +
//                        entity.getMeal_table().getName() + "\n" +
//                        entity.getQuantity() + " x " + entity.getMeal_table().getPrice() + " = " +
//                        entity.getMeal_table().getPrice() * entity.getQuantity(),
//                Button.markup(Button.rowList(Button.row(
//                                Button.button(Constant.back),
//                                Button.button(Constant.tozalash)
//                        ),
//                        Button.row(Button.button(Constant.formalization))
//                ))
//        ));
    }

    public TelegramUsers saveUser(Long chatId) {

        for (TelegramUsers users : usersList) {
            if (users.getChatId().equals(chatId)) {
                return users;
            }
        }


        TelegramUsers users = new TelegramUsers();
        users.setChatId(chatId);
        usersList.add(users);

        return users;
    }

    public void setLocationToOrder(Message message) {
        TelegramUsers step = saveUser(message.getChatId());
        if (!step.getStep().equals(Step.DELIVERY)) {
            return;
        }

        Location location = message.getLocation();
        ordersService.setLocation(message.getChatId(), location.getLatitude(), location.getLongitude());
        confirm(message);

        step.setStep(Step.CONFIRM);
    }
}
