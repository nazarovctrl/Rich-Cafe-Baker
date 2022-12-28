package com.example.admin.service;

import com.example.admin.repository.AdminMenuRepository;
import com.example.entity.MenuEntity;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.utill.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static com.example.utill.SendMsg.sendMsgParse;

@Service
public class MainMenuService {
    private final MyTelegramBot myTelegramBot;
    private final AdminMenuRepository adminMenuRepository;

    @Autowired
    @Lazy
    public MainMenuService(MyTelegramBot mainController, AdminMenuRepository adminMenuRepository) {
        this.myTelegramBot = mainController;
        this.adminMenuRepository = adminMenuRepository;
    }


    public void mainMenu(Message message) {
        myTelegramBot.send(sendMsgParse(message.getChatId(),
                "*Assalomu alekum admin panelga xush kelibsiz*",
                Button.markup(
                        Button.rowList(
                                Button.row(Button.button(Constant.addMenu),
                                        Button.button(Constant.attachMealToMenu)),

                                Button.row(Button.button(Constant.deleteMeal),
                                        Button.button(Constant.editePrice)),

                                Button.row(

                                        Button.button(Constant.settings),
                                        Button.button(Constant.deleteMenu))

                        )
                )));

    }

    public void menu(Message message) {

//
//        List<MenuEntity> entityList = adminMenuRepository.findAll();
//
//        if (entityList.size() == 0) {
//            myTelegramBot.send(sendMsgParse(message.getChatId(),
//                    "*Kechirasiz ! Hozircha menu lar ro'yxati mavjud eams !*"));
//        } else {
//
//            myTelegramBot.send(sendMsgParse(message.getChatId(),
//                    "*Taom biriktiriladigan menu ni tanlang !*",
//                    Button.markupMenu(entityList)));
//
//        }


    }
}
