package com.example.admin.service;
import com.example.admin.repository.SupplierRepostoriy;
import com.example.entity.AdminEntity;
import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.utill.Button;
import com.example.utill.IdCheckUtil;
import com.example.utill.SendMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    @Autowired
    private MyTelegramBot myTelegramBot;
    @Autowired
    private SupplierRepostoriy supplierRepostoriy;


    public void addSuplierFullName(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qo'shmoqchi bo'lgan Dastavchik Ism va Familyasini kiriting  ⬇"));
    }

    public void addSupplierPassword(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qo'shmoqchi bo'lgan Dastavchik ga Parol kiriting ( 8 - xonali )  ⬇"));
    }

    public void addSupplierPhone(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "Qo'shmoqchi bo'lgan ADMIN telefon raqamini kiriting Masalan (+998951024055)  ⬇"));
    }

    public boolean checkPhpone(Message message) {
        String text = message.getText();
        for (int i = 0; i < text.length(); i++) {
            if (!text.startsWith("+998") || (text.length() != 13)) {
                myTelegramBot.send(SendMsg.sendMsgParse(message.getChatId(),
                        "❌  Telefon nomer xato kiritildi !"+"\n"+
                                "✅ Iltimos telefon raqamni quyidagi shakilda jo'nating Masalan : (+998971234567)"));
                return false;
            }
        }
        return true;
    }

    public boolean checkPassword(Message message) {


        if (message.getText().length() < 8 || message.getText().length() > 8) {
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(), "" +
                    "❌  Kechirasiz Dastavchik ga beriladigan Parol 8 xonali bo'lsin. " + "\n" +
                    "✅  Qaytadan urining Masalan Parol (12013409)." + "\n" +
                    "❌  Eslatma Parol boshqa Dastavchik Paroli bilan takrorlanmasin. "));
            return false;
        }
        return true;
    }


    public void settingsMenu(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "✅  Dastavchik qabul qilindi keyingi amalni bajarishingiz mumkun ", Button.markup(
                        Button.rowList(Button.row(Button.button(Constant.backSettingsPanel)))
                )));
    }

    public boolean supplierList(Message message) {

        List<AdminEntity> adminEntityList = supplierRepostoriy.findByRole(UserRole.SUPPLIER);

        if(adminEntityList.isEmpty()){
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌  Kechirasiz xali Dastavchik mavjud emas !"));
            return false;
        }

        for (AdminEntity adminEntity : adminEntityList) {

            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "\uD83C\uDD94  ID : "+adminEntity.getId()+"\n" +
                            "\uD83D\uDCCC  FullName : "+adminEntity.getFullname()+"\n" +
                            "\uD83D\uDD11  Password : "+adminEntity.getPassword()+"\n" +
                            "\uD83D\uDCDE  Phone : "+adminEntity.getPhone()+"\n" +
                            "\uD83D\uDD30  Role : "+adminEntity.getRole()));
        }
        return true;
    }

    public void deleteSupplierMessage(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "O'chirmoqchi bo'lgan Dastavchik ID raqamini kiriting  ⬇"));
    }

    public boolean deleteSupplierById(Message message) {

      if (!IdCheckUtil.check(message.getText())){
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
            "Id to'g'ri kiriting "));
            return false;
        }

       Optional<AdminEntity> optional= supplierRepostoriy.findById(Integer.valueOf(message.getText()));

        if(optional.isEmpty()){
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌  Kechirasiz bunday ID mavjud emas qaytadan urining !"));
            return false;
        }

        supplierRepostoriy.deleteById(optional.get().getId());
        return true;
    }

    public void deleteSupplierGotovo(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "\uD83D\uDDD1  Dastavchik o'chirildi keyingi amalni bajarishingiz mumkun ", Button.markup(
                        Button.rowList(Button.row(Button.button(Constant.backSettingsPanel)))
                )));
    }

    public void supplierListMessage(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "\uD83D\uDCD6 Dastavchiklar ro'yxati : "));
    }

    public void supplierListGotovo(Message message) {

        myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                "✅  Keyingi amalni bajarishingiz mumkun ", Button.markup(
                        Button.rowList(Button.row(Button.button(Constant.backSettingsPanel)))
                )));
    }

    public boolean checkPasswordDataBase(Message message) {

        Optional<AdminEntity> optional = supplierRepostoriy.findByPassword(message.getText());

        if(optional.isPresent()){
            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌ Kechirasiz bunday parol bor qaytadan urining"));
            return true;
        }
        return false;
    }

    public boolean chackPhoneDataBase(Message message) {

        Optional<AdminEntity> optional = supplierRepostoriy.findByPhone(message.getText());

        if(optional.isPresent()){

            myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "❌ Kechirasiz bunday telefon raqam bor qaytadan urining"));
            return true;
        }
        return false;
    }
}
