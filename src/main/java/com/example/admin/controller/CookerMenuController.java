package com.example.admin.controller;

import com.example.admin.repository.AdminRepository;
import com.example.entity.AdminEntity;
import com.example.enums.UserRole;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.utill.Button;
import com.example.utill.IdCheckUtil;
import com.example.utill.SendMsg;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;

@Controller
public class CookerMenuController {

    private final MyTelegramBot myTelegramBot;

    private final AdminRepository adminRepository;

    public CookerMenuController(MyTelegramBot myTelegramBot, AdminRepository adminRepository) {
        this.myTelegramBot = myTelegramBot;
        this.adminRepository = adminRepository;
    }

    public void nameCooker(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qo'shmoqchi bo'lgan Zabzalni ismi va familiyasini kiriting "));
    }

    public void addPasswordCooker(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qo'shmoqchi bo'lgan Zabzal ga Parol kiriting Parol eng kamida 6 ta belgidan iborat bo'lsin  ⬇"));
    }

    public boolean checkPassword(Message message) {


        if (message.getText().length() <= 5) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(), """
                    ❌  Kechirasiz Zabzal ga beriladigan Parol eng kamida 6 ta belgidan iborat bo'lsin.\s
                    ✅  Qaytadan urining Masalan Parol (12345).
                    ❌  Eslatma Parol boshqa Zabzal Paroli bilan takrorlanmasin.\s"""));
            return false;
        }
        return true;
    }

    public void addCookerPhone(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qo'shmoqchi bo'lgan Zabzal telefon raqamini kiriting Masalan (+998991234567)  ⬇"));
    }

    public boolean checkPhone(Message message) {
        String text = message.getText();
        if (!text.startsWith("+998") || text.length() != 13) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌  Telefon nomer xato kiritildi ! \n" +
                            " ✅ Iltimos telefon raqamni quyidagi shaklda jo'nating Masalan : (+998991234567)"));
            return false;
        }
        return true;
    }

    public boolean deleteCookerById(Message message) {

        if (!IdCheckUtil.check(message.getText())) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Id ni to'g'ri kiriting "));
            return false;
        }

        Optional<AdminEntity> optional = adminRepository.findById(Integer.valueOf(message.getText()));

        if (optional.isEmpty()) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌  Kechirasiz bunday ID mavjud emas qaytadan urining !"));
            return false;
        }

        adminRepository.deleteById(optional.get().getId());
        return true;
    }

    public boolean cookerList(Message message) {

        List<AdminEntity> adminEntityList = adminRepository.findByRoleAndVisible(UserRole.COOKER, true);

        if (adminEntityList.isEmpty()) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌  Kechirasiz xali Zabzal mavjud emas !"));
            return false;
        }

        for (AdminEntity adminEntity : adminEntityList) {

            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "\uD83C\uDD94  ID : " + adminEntity.getId() + "\n" +
                            "\uD83D\uDCCC  FullName : " + adminEntity.getFullname() + "\n" +
                            "\uD83D\uDD11  Password : " + adminEntity.getPassword() + "\n" +
                            "\uD83D\uDCDE  Phone : " + adminEntity.getPhone() + "\n" +
                            "\uD83D\uDD30  Role : " + adminEntity.getRole()));
        }
        return true;
    }

    public void deleteCookerMessage(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "O'chirmoqchi bo'lgan Zabzal ID raqamini kiriting  ⬇"));
    }

    public void deletedCooker(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "\uD83D\uDDD1  Zabzal o'chirildi keyingi amalni bajarishingiz mumkun ", Button.markup(
                        Button.rowList(Button.row(Button.button(Constant.backSettingsPanel)))
                )));
    }
}
