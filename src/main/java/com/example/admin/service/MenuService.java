package com.example.admin.service;

import com.example.myTelegramBot.MyTelegramBot;
import com.example.utill.Button;
import com.example.interfaces.Constant;
import com.example.utill.SendMsg;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class MenuService {

    private final MyTelegramBot myTelegramBot;

    public MenuService(MyTelegramBot myTelegramBot) {
        this.myTelegramBot = myTelegramBot;
    }

    public void getNameOfMenu(Message message) {
        myTelegramBot.send(SendMsg.sendMsgParse(message.getChatId(),
                "Iltimos menu ga nom bering ! " +
                        "\n*Masalan : Ichimliklar * \uD83E\uDD64"));
    }


    public void saveMenu(Message message) {
        myTelegramBot.send(SendMsg.sendMsgParse(message.getChatId(),
                "*Muvaffaqqiyatli saqlandi ✅* ",
                Button.markup(Button.rowList(Button.row(Button.button(Constant.backMenu))))));
    }
}
