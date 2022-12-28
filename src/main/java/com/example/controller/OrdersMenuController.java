package com.example.controller;

import com.example.entity.MealEntity;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.service.MealsService;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;

@Controller
public class OrdersMenuController {



    public final MenuController menuController;

    private final MealsService mealsService;



    private final MyTelegramBot myTelegramBot;

    @Lazy
    public OrdersMenuController(MenuController menuController, MealsService mealsService, MyTelegramBot myTelegramBot) {
        this.menuController = menuController;
        this.mealsService = mealsService;
        this.myTelegramBot = myTelegramBot;
    }




    public void findMenuName(Message message) {
        List<MealEntity> mealEntities = mealsService.findNameList(message.getText());


        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(), "Uzingizga kerakli taomni tanlang ",
                Button.markupMeal(mealEntities, Button.rowList(
                        Button.row(Button.button(Constant.savat)),
                        Button.row(Button.button(Constant.back), Button.button(Constant.home))
                ))
        ));

    }

    public void findMenuName(Message message,String text) {
        List<MealEntity> mealEntities = mealsService.findNameList(text);


        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(), "Uzingizga kerakli Menyuni tanlang ",
                Button.markupMeal(mealEntities, Button.rowList(
                        Button.row(Button.button(Constant.savat)),
                        Button.row(Button.button(Constant.back), Button.button(Constant.home))
                ))
        ));

    }

    public void findMenuMeal(Message message) {
        Optional<MealEntity> optional = mealsService.findName(message.getText());


        if (optional.isEmpty()){
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Iltimos qaytadan urinib koring"));
        }
        MealEntity meal = optional.get();

        myTelegramBot.send(SendMsg.sendPhoto(message.getChatId(),
                "Mahsulot nomi: "+meal.getName()+"\n\n" +
                        "narxi: "+meal.getPrice(),
                meal.getPhoto()));

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(), "Miqdorni tanlang ",
                Button.buttonCount(Button.rowList(
                        Button.row(Button.button(Constant.back), Button.button(Constant.home))
                ))
        ));

    }


}
