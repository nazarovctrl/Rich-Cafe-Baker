package com.example.admin.controller;

import com.example.admin.service.AdminAuthService;
import com.example.entity.AdminEntity;
import com.example.enums.Step;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.step.Admin;
import com.example.step.TelegramUsers;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminAuthController {

    private final MyTelegramBot myTelegramBot;

    private final AdminAuthService authService;
    private List<Admin> usersList = new ArrayList<>();

    @Lazy
    public AdminAuthController(MyTelegramBot myTelegramBot, AdminAuthService authService) {
        this.myTelegramBot = myTelegramBot;
        this.authService = authService;
    }

    public void handle(Message message) {

        Admin users = saveUser(message.getChatId());
        TelegramUsers stepMain = myTelegramBot.saveUser(message.getChatId());

        if (message.hasText()) {

            String text = message.getText();
            if (users.getStep() == null || text.equals("%login77#")) {
                myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                        "Ruyhatdan o'tkazilgan telefon raqamingizni yuboring (+998991234567) ",
                        Button.markup(Button.rowList(Button.row(
                                Button.button()
                        )))));

                users.setStep(Step.PHONE);
                return;
            }

            if (users.getStep().equals(Step.PHONE)) {

                if (!checkPhone(message.getText())) {
                    myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                            "Iltimos telefon raqamni to'g'ri kiriting ! "));
                    return;
                }

                if (checkPhoneExists(message.getText())) {
                    myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                            "Bu raqam ruyhatdan o'tkazilmagan \n" +
                                    "Iltimos qaytadan kiriting "));
                    return;

                }

                ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
                keyboardRemove.setRemoveKeyboard(true);

                SendMessage sendMessage = new SendMessage();
                sendMessage.setText("Sizga berilgan parolni kiriting");
                sendMessage.setChatId(message.getChatId());
                sendMessage.setReplyMarkup(keyboardRemove);
                myTelegramBot.send(sendMessage);

                users.setStep(Step.ADMIN_PASSWORD);
                users.setPhone(message.getText());
                return;
            }

            if (users.getStep().equals(Step.ADMIN_PASSWORD)) {

                AdminEntity adminEntity = authService.findByPhone(users.getPhone());

                if (!adminEntity.getPassword().equals(message.getText())) {
                    myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                            "Parolni xato kiritdingiz iltimos qaytadan urinib koring ! "));
                    return;
                }

                adminEntity.setUserId(message.getChatId());
                authService.saveUserId(adminEntity);
                stepMain.setStep(Step.START);
                myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                        "Muvaffaqiyatli ruyhatdan o'tdingiz",
                        Button.markup(Button.rowList(
                                Button.row(Button.button("Asosiy Menyu !"))
                        ))));
                users.setStep(null);
            }

        }
        if (message.hasContact() && users.getStep().equals(Step.PHONE)) {

            String phone = message.getContact().getPhoneNumber();
            if (checkPhoneExists(phone)) {
                myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                        "Bu raqam ruyhatdan o'tkazilmagan \n" +
                                "Iltimos qaytadan kiriting "));
                return;
            }

            ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
            keyboardRemove.setRemoveKeyboard(true);

            SendMessage sendMessage = new SendMessage();
//          sendMessage.setText("Sizga berilgan parolni kiriting");

            sendMessage.setChatId(message.getChatId());
            sendMessage.setReplyMarkup(keyboardRemove);
            myTelegramBot.send(sendMessage);

            users.setStep(Step.ADMIN_PASSWORD);
            users.setPhone(phone);


        }
    }

    public boolean checkPhoneExists(String text) {
        return !authService.isExists(text);
    }

    public boolean checkPhone(String text) {
        return text.startsWith("+998") && text.length() == 13 || text.startsWith("998") && text.length() == 12;
    }


    public Admin saveUser(Long chatId) {

        for (Admin users : usersList) {
            if (users.getChatId().equals(chatId)) {
                return users;
            }
        }


        Admin users = new Admin();
        users.setChatId(chatId);
        usersList.add(users);

        return users;
    }
}

