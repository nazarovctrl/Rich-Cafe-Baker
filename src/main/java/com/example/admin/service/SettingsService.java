package com.example.admin.service;

import com.example.admin.repository.AdminRepository;
import com.example.entity.AdminEntity;
import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.utill.Button;
import com.example.utill.SendMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;

@Service
public class SettingsService {

    private final MyTelegramBot myTelegramBot;
    private final AdminRepository adminRepostoriy;

    @Lazy
    public SettingsService(MyTelegramBot myTelegramBot, AdminRepository adminRepostoriy) {
        this.myTelegramBot = myTelegramBot;
        this.adminRepostoriy = adminRepostoriy;
    }

    public void addAdminFullName(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qo'shmoqchi bo'lgan ADMIN Ism va Familyasini kiriting  ⬇"));

    }

    public void addAdminPassword(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qo'shmoqchi bo'lgan ADMIN ga Parol kiriting ( 8 - xonali )  ⬇"));


    }

    public void addAdminPhone(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qo'shmoqchi bo'lgan ADMIN telefon raqamini kiriting Masalan (+998951024055)  ⬇"));
    }

    public boolean checkPhoneNumber(Message message) {
        String text = message.getText();
        for (int i = 0; i < text.length(); i++) {
            if (!text.startsWith("+998") || (text.length() != 13)) {
                myTelegramBot.send(SendMsg.sendMsgParse(message.getChatId(),
                        "❌  Telefon nomer xato kiritildi !" + "\n" +
                                "✅ Iltimos telefon raqamni quyidagi shakilda jo'nating Masalan : (+998971234567)"));
                return false;
            }
        }
        return true;
    }

    public boolean checkpassword(Message message) {

        if (message.getText().length() < 8 || message.getText().length() > 8) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(), "" +
                    "❌  Kechirasiz ADMIN ga beriladigan Parol 8 xonali bo'lishi kerak " + "\n" +
                    "✅  Qaytadan urining Masalan Parol (12013409)" + "\n" +
                    "❌  Eslatma Parol boshqa ADMIN Paroli bilan takrorlanmasin ! "));
            return false;
        }
        return true;
    }

    public void settingsMenu(Message message) {
        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "✅  ADMIN qabul qilindi keyingi amalni bajarishingiz mumkun ", Button.markup(
                        Button.rowList(Button.row(Button.button(Constant.backSettingsPanel)))
                )));
    }

    public boolean adminList(Message message) {

        List<AdminEntity> adminEntityList = adminRepostoriy.findByRole(UserRole.ADMIN);

        if (adminEntityList.isEmpty()) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌  Kechirasiz xali ADMIN mavjud emas !"));
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

    public void deleteAdmin(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "O'chirmoqchi bo'lgan ADMIN ID raqamini kiriting  ⬇"));
    }

    public boolean deleteAdminById(Message message) {
        Optional<AdminEntity> optional = null;
        try {
            optional = adminRepostoriy.findById(Integer.valueOf(message.getText()));

        }catch (RuntimeException e){
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Admin Idsini to'g'ri kiriting "));
            return false;
        }

        if (optional.isEmpty()) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌  Kechirasiz bunday ID mavjud emas qaytadan urining !"));
            return false;
        }

        adminRepostoriy.deleteById(optional.get().getId());
        return true;
    }

    public void deleteAdminGotovo(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "\uD83D\uDDD1  ADMIN o'chirildi keyingi amalni bajarishingiz mumkun ", Button.markup(
                        Button.rowList(Button.row(Button.button(Constant.backSettingsPanel)))
                )));
    }

    public void adminListMessage(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "\uD83D\uDCD6  ADMIN lar ro'yxati : "));
    }

    public void adminListGotovo(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "✅ Keyingi amalni bajarishingiz mumkun ", Button.markup(
                        Button.rowList(Button.row(Button.button(Constant.backSettingsPanel)))
                )));

    }

    public boolean checkpasswordDataBase(Message message) {

        Optional<AdminEntity> optional = adminRepostoriy.findByPassword(message.getText());

        if (optional.isPresent()) {

            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌ Kechirasiz bunday Parol bor qaytadan urining"));
            return true;
        }
        return false;
    }

    public boolean checkPhoneDataBase(Message message) {

        Optional<AdminEntity> optional = adminRepostoriy.findByPhone(message.getText());

        if (optional.isPresent()) {

            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌ Kechirasiz bunday telefon raqam bor qaytadan urining"));
            return true;
        }
        return false;
    }

    public List<AdminEntity> getAdminList() {
        return adminRepostoriy.findByRole(UserRole.ADMIN);
    }

    public boolean isAdmin(Long userId) {
        return adminRepostoriy.existsByUserIdAndRole(userId,UserRole.ADMIN);
    }
}
