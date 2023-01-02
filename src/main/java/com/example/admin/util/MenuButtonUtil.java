package com.example.admin.util;

import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.meta.api.objects.Message;

import static com.example.utill.SendMsg.sendMsgParse;

@Component
public class MenuButtonUtil {

    private final MyTelegramBot myTelegramBot;

    public MenuButtonUtil(MyTelegramBot myTelegramBot) {
        this.myTelegramBot = myTelegramBot;
    }


    public void mainMenu(Message message) {
        myTelegramBot.send(sendMsgParse(message.getChatId(),
                "*Assalomu alekum admin panelga xush kelibsiz*",
                Button.markup(
                        Button.rowList(
                                Button.row(Button.button(Constant.addMenu), Button.button(Constant.attachMealToMenu)),
                                Button.row(Button.button(Constant.deleteMenu), Button.button(Constant.deleteMeal)),
                                Button.row(Button.button(Constant.menuList), Button.button(Constant.mealsList)),
                                Button.row(Button.button(Constant.settings), Button.button(Constant.editePrice))

                        )
                )));

    }

    public void settingMenu(Message message) {
        myTelegramBot.send(SendMsg.sendMsgMark(message.getChatId(), "*Sozlamalar paneliga xush kelibsiz*",
                Button.markup(Button.rowList(
                        Button.row(Button.button(Constant.addAdmin), Button.button(Constant.deleteAdmin)),
                                Button.row(Button.button(Constant.listOfAdmin)),
                        Button.row(Button.button(Constant.addSupplier),Button.button(Constant.deleteSupplier)),
                                Button.row(Button.button(Constant.supplierList)),
                        Button.row(Button.button(Constant.addCooker),Button.button(Constant.deleteCooker)),
                                Button.row(Button.button(Constant.listCooker)),
                        Button.row(Button.button(Constant.backMenu))
                ))
        ));
    }


}
