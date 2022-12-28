package com.example.admin.service;

import com.example.entity.AdminEntity;
import com.example.admin.repository.AdminRepository;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.utill.Button;
import com.example.interfaces.Constant;
import com.example.utill.SendMsg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.*;
import java.util.*;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    private final MyTelegramBot mainController;

    public AdminService(AdminRepository adminRepository, MyTelegramBot mainController) {
        this.adminRepository = adminRepository;
        this.mainController = mainController;
    }


    public void getNameOfAdmin(Message message) {
        mainController.send(SendMsg.sendMsgParse(message.getChatId(),
                "Iltimos Adminning ismi va familiyasini kiriting" +
                        "*\nMasalan Ali aliyev*"));
    }

    public void getphoneOfAdmin(Message message) {
        mainController.send(SendMsg.sendMsgParse(message.getChatId(),
                "Iltimos Adminning telefon raqamini kiriting" +
                        "*\nMasalan : +998971234567*"));
    }

    public void saveOfAdmin(Message message) {
        mainController.send(SendMsg.sendMsgParse(message.getChatId(),
                "Muvaffaqqiyatli saqlandi ✅", Button.markup(
                        Button.rowList(Button.row(Button.button(Constant.back)))
                )));
    }

    public boolean checkPhoneNumber(Message message) {
        String text = message.getText();
        for (int i = 0; i < text.length(); i++) {
            if (!text.startsWith("+998") || (text.length() != 13)) {
                mainController.send(SendMsg.sendMsgParse(message.getChatId(),
                        "Iltimos telefon raqamni quyidagi shakilda jo'nating !" +
                                "\n +998971234567"));
                return false;
            }
        }

        return true;
    }

    public boolean existByPhone(Message message) {
        boolean existsByPhone = adminRepository.existsByPhone(message.getText());
        if (existsByPhone) {
            mainController.send(SendMsg.sendMsgParse(message.getChatId(),
                    "*Bunday telefon raqam ba'zada  mavjud !*", Button.markup(
                            Button.rowList(Button.row(Button.button(Constant.back)))
                    )));
            return true;
        }

        return false;
    }

    public void listOfAdmins(Message message) {

        boolean check = false;

        List<AdminEntity> adminEntityList = adminRepository.findAll();

        Map<Integer, Object[]> patientData = new TreeMap<Integer, Object[]>();

        patientData.put(0, new Object[]{"ID raqami ", " Ism va Familiyasi", "Telefon raqami"});

        for (AdminEntity adminEntity : adminEntityList) {

            if (adminEntity != null) {

                check = true;

                XSSFWorkbook workbook = new XSSFWorkbook();
                XSSFSheet spreadsheet = workbook.createSheet("Adminlar royhati");

                XSSFRow row;

                patientData.put(adminEntity.getId(), new Object[]{adminEntity.getId().toString(), adminEntity.getFullname(),
                        adminEntity.getPhone()});
                Set<Integer> keyid = patientData.keySet();

                int rowid = 0;
                for (Integer key : keyid) {
                    row = spreadsheet.createRow(rowid++);
                    Object[] objectArr = patientData.get(key);
                    int cellid = 0;

                    for (Object obj : objectArr) {
                        Cell cell = row.createCell(cellid++);
                        cell.setCellValue((String) obj);
                    }

                }

                try {

                    FileOutputStream out = new FileOutputStream("Adminlar ro`yxati.xlsx");
                    workbook.write(out);
                    out.close();


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (!check) {

            mainController.send(SendMsg.sendMsgParse(message.getChatId(),
                    "*Adminlar ro'yxati mavjud emas*"
            ));
        } else {
            try {
                InputStream inputStream = new FileInputStream("Adminlar ro`yxati.xlsx");
                InputFile inputFile = new InputFile();
                inputFile.setMedia(inputStream, "Adminlar ro`yxati.xlsx");

                mainController.send(SendMsg.sendAdminDoc(message.getChatId(), inputFile
                ));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void deleteByPhone(Message message) {
        boolean phone = checkPhoneNumber(message);
        if (phone) {
            Optional<AdminEntity> optional = adminRepository.findByPhone(message.getText());
            if (optional.isEmpty()) {
                mainController.send(SendMsg.sendMsgParse(message.getChatId(),
                        "*Bunday telefon raqam ba'zada mavju emas*",
                        Button.markup(Button.rowList(Button.row(Button.button(Constant.back))))));
            } else {
                AdminEntity adminEntity = optional.get();
                try {
                    adminRepository.delete(adminEntity);
                    mainController.send(SendMsg.sendMsgParse(message.getChatId(),
                            "*Muvaffaqqiyatli o'chirildi *",
                            Button.markup(Button.rowList(Button.row(Button.button(
                                    Constant.back
                            ))))));
                } catch (Exception e) {
                    mainController.send(SendMsg.sendMsgParse(message.getChatId(),
                            "*Bunday telefon raqam ba'zada mavju emas*",
                            Button.markup(Button.rowList(Button.row(Button.button(Constant.back))))));
                }

            }
        }
    }
}
