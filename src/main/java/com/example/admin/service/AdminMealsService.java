package com.example.admin.service;

import com.example.admin.repository.AdminMealsRepository;
import com.example.admin.repository.AdminMenuRepository;
import com.example.entity.MealEntity;
import com.example.entity.MenuEntity;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;

@Service
public class AdminMealsService {

    @Autowired
    private MyTelegramBot myTelegramBot;
    @Autowired
    private AdminMenuRepository menuRepostoriy;
    @Autowired
    private AdminMealsRepository mealsRepostoriy;

    public void mealsPrice(Message message){
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qo'shayotgan maxsulotni narxini kiriting ( sumda )  ⬇"));
    }
    public void mealsName(Message message){
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qoshayotgan maxsulot nomini kiriting emodji bilan \uD83D\uDE0A"+"\n" +
                        "✅ Masalan : \uD83C\uDF62 Tovuq shashlik"));
    }
    public boolean menuList(Message message){

        List<MenuEntity> entityList =  menuRepostoriy.findAll();

        if(entityList.isEmpty()){
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Kechirasiz xali Menular mavjud emas  ❌"));
            return false;
        }


        for (MenuEntity menuEntity : entityList) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "\uD83C\uDD94  Menu ID raqami  :  "+menuEntity.getId()+"\n" +
                            "\uD83D\uDCCC Menu nomi  :  "+menuEntity.getName()));

        }
        return true;
    }
    public void addMealsMenu(Message message){

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qoshmoqchi bo'lgan maxsulot qaysi menuga tegishli korsatish uchun menu ID raqamini kiriting  ⬇"));
    }
    public Optional<MenuEntity> returnetMenuId(Message message){

        Optional<MenuEntity> optional = menuRepostoriy.findById(Integer.valueOf(message.getText()));

        if(optional.isEmpty()){
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌  Kechirasiz Menular bazasida bunday ID mavjud emas boshqatan urinig !"));

        }
       return optional;
    }
    public boolean mealesList(Message message){

        List<MealEntity> entityList =  mealsRepostoriy.findByMenu_Id(Integer.valueOf(message.getText()));

        if(entityList.isEmpty()){
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌  Kechirasiz xali bu Menuda maxsulot mavjud emas boshqa amal bajaring ! "));
            return false;
        }


        for (MealEntity mealEntity : entityList) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "\uD83C\uDD94  Maxsulot ID raqami  :  "+mealEntity.getId()+"\n" +
                            "\uD83D\uDCCC Maxsulot nomi  :  "+mealEntity.getName()));

        }

        return true;
    }
    public void deleteMeals1(Message message){

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "O'chirmoqchi bo'lgan maxsulot qaysi Menuga tegishli ekanligini bilish uchun Menu ID raqamini kiriting ⬇"));
    }
    public void deleteMeal2(Message message){

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Ochirmoqchi bo'lgan maxulot ID raqamini kiriting  ⬇"));
    }
    public boolean deleteMeal3(Message message) {

        Optional<MealEntity> optional = mealsRepostoriy.findById(Integer.valueOf(message.getText()));
        if (optional.isEmpty()) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌ Kechirasiz bunaqa ID mavjud emas boshqatan urining ! "));
            return false;
        }

        mealsRepostoriy.deleteById(Math.toIntExact(optional.get().getId()));
        return true;
    }
    public void saveMenu(Message message) {
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Muvaffaqqiyatli O'chirildi keyingi amalni bajarishingiz mumkun ✅ ",
                Button.markup(Button.rowList(Button.row(Button.button(Constant.backMenu))))));
    }
    public void updateMealsPrice1(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Narxini o'zgartirmoqchi bo'lgan maxsulot qaysi Menuga tegishli ekanligini bilish uchun Menu ID raqamini kiriting ⬇"));
    }
    public boolean mealesListUpdate(Message message){

        List<MealEntity> entityList =  mealsRepostoriy.findByMenu_Id(Integer.valueOf(message.getText()));

        if(entityList.isEmpty()){
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌ Kechirasiz xali Maxsulotlar mavjud emas boshqatan urining "));
            return false;
        }


        for (MealEntity mealEntity : entityList) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "\uD83C\uDD94   Maxsulot ID raqami   :  "+mealEntity.getId()+"\n" +
                            "\uD83D\uDCCC   Maxsulot nomi  :  "+mealEntity.getName()+"\n"+
                            "\uD83D\uDCB8   Maxsulot narxi  :  "+mealEntity.getPrice()));

        }
        return true;
    }
    public void updateMeals(Message message){

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Narxini o'zgartirmoqchi bo'lgan maxsulot ID raqamini kiriting  ⬇"));
    }
    public Optional<MealEntity> updateMealsById(Message message) {

        Optional<MealEntity> optional = mealsRepostoriy.findById(Integer.valueOf(message.getText()));
        if(optional.isEmpty()){
            return null;
        }
        return optional;
    }
    public void updatePriceMessage(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Muvaffaqqiyatli Narx o'zgardi ✅ ",
                Button.markup(Button.rowList(Button.row(Button.button(Constant.backMenu))))));
    }
    public void addPrice(Message message) {

        myTelegramBot.send(SendMsg.sendMsgParse(message.getChatId(),
                "Maxsulot topildi o'zgartirmoqchi bo'lgan narxni kiriting   ⬇"));
    }
    public void nullMealsEntity(Message message) {

        myTelegramBot.send(SendMsg.sendMsgParse(message.getChatId(),
                "❌  Kechirasiz bunaqa ID mavjud emas qayatdab urining ! "));

    }
    public void MealsByIdList(Message message,MealEntity meals) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "\uD83C\uDD94  Maxsulot ID raqami  :  " + meals.getId()+"\n" +
                        "\uD83D\uDCCC  Maxsulot nomi  :  "+meals.getName()+"\n" +
                        "\uD83D\uDCB8  Maxsulot narxi  :  "+meals.getPrice()));

    }
    public void mealsListMessage(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Ko'rmoqchi bo'lgan maxsulot qaysi menuga tegishli ekanligini"+"\n" +
                        "bilish uchun Menu ID raqamini kiriting  ⬇"));
    }
    public boolean mealsListAdmin(Message message) {

        List<MealEntity> entityList =  mealsRepostoriy.findByMenu_Id(Integer.valueOf(message.getText()));

        if(entityList.isEmpty()){
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌ Kechirasiz xali bu Menuda  Maxsulotlar mavjud emas boshqatan urining ⬇ "));
            return false;
        }


        for (MealEntity mealEntity : entityList) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "\uD83C\uDD94   Maxsulot ID raqami   :  "+mealEntity.getId()+"\n" +
                            "\uD83D\uDCCC   Maxsulot nomi  :  "+mealEntity.getName()+"\n"+
                            "\uD83D\uDCB8   Maxsulot narxi  :  "+mealEntity.getPrice()));

        }
        return true;
    }
    public void melasListGotovo(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "✅ Keyingi amalni bajarishingiz mumkun ",
                Button.markup(Button.rowList(Button.row(Button.button(Constant.backMenu))))));

    }
    public void menuListGotovo(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "✅ Keyingi amalni bajarishingiz mumkun ",
                Button.markup(Button.rowList(Button.row(Button.button(Constant.backMenu))))));
    }

    public boolean checkMealsName(Message message) {

        Optional<MealEntity> optional = mealsRepostoriy.findByName(message.getText());

        if(optional.isPresent()){

            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌ Kechirasiz bunday maxsulot nomi bazada mavjud qaytadan urining !"));
            return false;
        }
        return true;
    }
}
