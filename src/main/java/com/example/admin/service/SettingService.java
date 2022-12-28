package com.example.admin.service;

import com.example.myTelegramBot.MyTelegramBot;
import com.example.utill.Button;
import com.example.interfaces.Constant;
import com.example.utill.SendMsg;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class SettingService {

    private final MyTelegramBot myTelegramBot;

    public SettingService(MyTelegramBot myTelegramBot) {
        this.myTelegramBot = myTelegramBot;
    }

    public void settingMenu(Message message) {
        myTelegramBot.send(SendMsg.sendMsgMark(message.getChatId(), "*Soslamalar paneliga xush kelibsiz*",
                Button.markup(Button.rowList(
                        Button.row(Button.button(Constant.addAdmin), Button.button(Constant.deleteAdmin)),
                        Button.row(
                                Button.button(Constant.backMenu),
                                Button.button(Constant.listOfAdmin))))));
    }
}
