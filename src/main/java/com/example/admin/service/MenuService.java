package com.example.admin.service;

import com.example.admin.repository.AdminMenuRepository;
import com.example.entity.MenuEntity;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.repository.MenuRepository;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

@Service
public class MenuService {


    @Autowired
    private AdminMenuRepository menuRepository;
    private final MyTelegramBot myTelegramBot;

    public MenuService(MyTelegramBot myTelegramBot) {
        this.myTelegramBot = myTelegramBot;
    }

    public void menuName(Message message){

       myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
               "Qo'shmoqchi bo'lgan Menu nomini kiriting emodji bilan \uD83D\uDE0A"+"\n" +
                       "✅ Masalan : \uD83C\uDF62 Shashliklar "));

    }
    public void saveMenu(Message message) {
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Menu qo'shildi keyingi amalni bajarishingiz mumkun  ✅ ",
                Button.markup(Button.rowList(Button.row(Button.button(Constant.backMenu))))));
    }
    public void saveMenuDelete(Message message) {
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Menu o'chirildi keyingi amalni bajarishingiz mumkun  ✅ ",
                Button.markup(Button.rowList(Button.row(Button.button(Constant.backMenu))))));
    }
    public void deleteMeals1(Message message){

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "\uD83D\uDDD1  O'chirmoqchi bo'lgan Menu ID raqamini kiriting ⬇"));
    }
    public boolean deleteMeal3(Message message) {

        Optional<MenuEntity> optional =menuRepository.findById(Integer.valueOf(message.getText()));
        if (optional.isEmpty()) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Kechirasiz bunaqa ID mavjud emas boshqatan urining  ❌ "));
            return false;
        }
        menuRepository.deleteById(Math.toIntExact(optional.get().getId()));
        return true;
    }


    public boolean checkMenuName(Message message) {

        Optional<MenuEntity> optional = menuRepository.findByName(message.getText());

        if(optional.isPresent()){

            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌ Kechirasiz bunaqa Menu nomi bor qaytadan urining"));
            return true;
        }
        return false;
    }
}
