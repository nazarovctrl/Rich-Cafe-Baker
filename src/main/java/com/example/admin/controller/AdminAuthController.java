package com.example.admin.controller;

import com.example.admin.service.AdminAuthService;
import com.example.entity.AdminEntity;
import com.example.enums.Step;
import com.example.enums.UserRole;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.step.TelegramUsers;
import com.example.utill.Button;
import com.example.utill.InlineButton;
import com.example.utill.SendMsg;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminAuthController {

    private final MyTelegramBot myTelegramBot;

    private final AdminAuthService authService;

    private List<TelegramUsers> usersList = new ArrayList<>();

    private AdminEntity adminEntity = new AdminEntity();
@Lazy
    public AdminAuthController(MyTelegramBot myTelegramBot, AdminAuthService authService) {
        this.myTelegramBot = myTelegramBot;
        this.authService = authService;
    }

    public void handle(Message message) {

    TelegramUsers users = saveUser(message.getChatId());
    TelegramUsers stepMain= myTelegramBot.saveUser(message.getChatId());

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

            if (!checkPhoneExists(message.getText())) {
                myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                        "Bu raqam ruyhatdan o'tkazilmagan \n" +
                                "Iltimos qaytadan kiriting "));
                return;
            }

            adminEntity = authService.findByPhone(message.getText());

            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Sizga berilgan parolni kiriting: "));

            users.setStep(Step.ADMIN_PASSWORD);
            return;
        }

        if (users.getStep().equals(Step.ADMIN_PASSWORD) && adminEntity!=null) {

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
            adminEntity = new AdminEntity();
        }
    }
    if (message.hasContact() && users.getStep().equals(Step.PHONE)){

        String phone = message.getContact().getPhoneNumber();
        if (!checkPhoneExists(phone)) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Bu raqam ruyhatdan o'tkazilmagan \n" +
                            "Iltimos qaytadan kiriting "));
            return;
        }

        adminEntity = authService.findByPhone(phone);

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Sizga berilgan parolni kiriting: "));

        users.setStep(Step.ADMIN_PASSWORD);

    }
}

    public boolean checkPhoneExists(String text) {
        return authService.isExists(text);
    }

    public boolean checkPhone(String text) {
        if (text.startsWith("+998") && text.length() == 13 || text.startsWith("998") && text.length() == 12) {
            return true;
        }
        return false;
    }




        public TelegramUsers saveUser (Long chatId){

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
    }

