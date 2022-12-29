package com.example.controller;

import com.example.entity.MenuEntity;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.repository.MenuRepository;
import com.example.service.AddMenuService;
import com.example.service.OrdersService;
import com.example.service.UserService;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import java.util.List;

@Controller
public class MenuController {


    @Autowired
    private MenuRepository repository;
    @Autowired
    private UserService userService;

    @Autowired
    private MyTelegramBot myTelegramBot;

    @Autowired
    private AddMenuService menuService;

    @Autowired
    private OrdersService ordersService;

    public void mainMenu(Message message) {

        //Asosiy Menyu
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Asosiy Menu", Button.markup(Button.rowList(Button.row(
                                Button.button(Constant.addOrder)
                        ),
                        Button.row(Button.button(Constant.buyurtmalar),
                                Button.button(Constant.settings)),
                        Button.row(Button.button(Constant.about),
                                Button.button(Constant.addComment))
                ))
        ));

    }

    public void mainMenu(Long chatId) {

        //Asosiy Menyu
        myTelegramBot.send(SendMsg.sendMsg(chatId,
                "Asosiy Menu", Button.markup(Button.rowList(Button.row(
                                Button.button(Constant.addOrder)
                        ),
                        Button.row(Button.button(Constant.buyurtmalar),
                                Button.button(Constant.settings)),
                        Button.row(Button.button(Constant.about),
                                Button.button(Constant.addComment))
                ))
        ));

    }


    public void settings(Message message) {
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                Constant.settings,
                Button.markup(Button.rowList(
                        Button.row(Button.button(Constant.phone)),
                        Button.row(Button.button(Constant.back))
                ))));
    }

    public void orderMenu(Message message) {


        List<MenuEntity> entityList = repository.findAll();


        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(), "Uzingizga kerakli Menyuni tanlang ",
                Button.markupMenu(entityList, Button.rowList(
                        Button.row(Button.button(Constant.savat)),
                        Button.row(Button.button(Constant.back), Button.button(Constant.home))
                ))
        ));


    }


    public void order(Message message) {
     ordersService.getOrdersHistory(message.getChatId());

    }

    public void about(Message message) {
        InputFile inputFile = new InputFile();
        inputFile.setMedia("BAACAgIAAxkBAAIBIWN3ECiYxRjsHybaa5924G6PwKSMAAKvIwACCkm5S98ZGmqaQU9iKwQ");
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(message.getChatId());
        sendVideo.setVideo(inputFile);
        sendVideo.setCaption("\uD83D\uDE0D Siz turk xalqining taomlarini sevuvchi insonlardan birimisiz? Ularni tez-tez tanovul qilishni xohlaysizmi?\n" +
                "\n" +
                "Unda Rich cafe & bakery aynan siz uchun. Bu yerda siz, turk taomlarini, desert-u shirinliklarini va albatta turk qahvasidan bahramand bo'lasiz.\n" +
                "\n" +
                "\uD83D\uDE07 Marosim va tadbirlarni ideal nishonlash uchun eng zo‘r tanlov bu Rich cafe & bakery'dir.\n" +
                "\n" +
                "\uD83D\uDCCDManzil: Qarshi shahri, 3-mikrorayon, Yubileyniy tabaka orqa tomoni.\n\n" +
                "@rich_bakery_cafe_bot");

        myTelegramBot.send(sendVideo);
    }

    public void contactButton(Message message) {
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(), "Iltimos telefon raqamingizni yuboring",
                Button.markup(Button.rowList(
                        Button.row(Button.button())
                ))
        ));
    }
}
