package com.example.admin.controller;

import com.example.admin.service.AdminService;
import com.example.admin.service.MainMenuService;
import com.example.admin.service.MenuService;
import com.example.admin.service.SettingService;
import com.example.entity.MenuEntity;
import com.example.entity.ProfileEntity;
import com.example.enums.Step;
import com.example.admin.repository.AdminMenuRepository;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.repository.ProfileRepository;
import com.example.step.TelegramUsers;
import com.example.utill.SendMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminController {



    private final List<TelegramUsers> usersList = new ArrayList<>();
    private final MainMenuService menuService;
    private final MyTelegramBot myTelegramBot;
    private final MenuService addMenuService;
    private final SettingService settingService;
    private final AdminMenuRepository adminMenuRepository;
    private final AdminService addAdminService;
    private final ProfileRepository adminRepository;

    private MenuEntity menuEntity = new MenuEntity();
    private ProfileEntity adminEntity = new ProfileEntity();

    @Autowired
    @Lazy
    public AdminController(MainMenuService menuService, MyTelegramBot myTelegramBot, MenuService addMenuService, SettingService settingService, AdminMenuRepository adminMenuRepository, AdminService addAdminService, ProfileRepository adminRepository) {
        this.menuService = menuService;
        this.myTelegramBot = myTelegramBot;
        this.addMenuService = addMenuService;
        this.settingService = settingService;
        this.adminMenuRepository = adminMenuRepository;
        this.addAdminService = addAdminService;
        this.adminRepository = adminRepository;
    }





    public void handle(Message message) {

        TelegramUsers user = saveUser(message.getChatId());

        if (message.hasText() && message.getText().equals("/start")) {
            menuService.mainMenu(message);
            return;
        }


        switch (message.getText()) {
            case Constant.addMenu -> {
                addMenuService.getNameOfMenu(message);
                user.setStep(Step.GET_NAME_OF_MENU);
                return;
            }


            case Constant.attachMealToMenu -> {
                menuService.menu(message);
                return;
            }

            case Constant.listOfAdmin -> {
                addAdminService.listOfAdmins(message);
                return;
            }

            case Constant.deleteMeal -> myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Taom o'chirish !"));

            case Constant.editePrice -> myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Taom qo'narxlarini ozgartirish !"));


            case Constant.deleteMenu -> myTelegramBot.send(SendMsg.sendMsg(message.getChatId(),
                    "Menuni o'chirish !"));


            case Constant.addAdmin -> {
                addAdminService.getNameOfAdmin(message);
                user.setStep(Step.GET_NAME_OF_ADMIN);
                return;
            }

            case Constant.deleteAdmin -> {
                addAdminService.getphoneOfAdmin(message);
                user.setStep(Step.DELETE_ADMIN_BY_PHONE);
                return;
            }

            case Constant.settings -> {
                settingService.settingMenu(message);
                return;
            }

            case Constant.backMenu -> {
                menuService.mainMenu(message);
                user.setStep(Step.MAIN);
                return;
            }

        }

        switch (user.getStep()) {
            case GET_NAME_OF_MENU -> {
                menuEntity.setName(message.getText());
                adminMenuRepository.save(menuEntity);
                addMenuService.saveMenu(message);
                user.setStep(Step.MAIN);
                menuEntity = new MenuEntity();


            }
            case GET_NAME_OF_ADMIN -> {
                adminEntity.setFullName(message.getText());
                addAdminService.getphoneOfAdmin(message);
                user.setStep(Step.GET_PHONE_OF_ADMIN);
            }
            case GET_PHONE_OF_ADMIN -> {
                boolean number = addAdminService.checkPhoneNumber(message);
                boolean phone = addAdminService.existByPhone(message);
                if (phone) {
                    return;
                }

                if (number) {
                    adminEntity.setPhone(message.getText());
                    adminRepository.save(adminEntity);
                    addAdminService.saveOfAdmin(message);
                    user.setStep(Step.MAIN);
                    adminEntity = new ProfileEntity();
                }
            }

            case DELETE_ADMIN_BY_PHONE -> addAdminService.deleteByPhone(message);

        }
    }


    public TelegramUsers saveUser(Long chatId) {

        for (TelegramUsers users : usersList) {
            if (users.getChatId().equals(chatId)) {
                return users;
            }
        }


        TelegramUsers users = new TelegramUsers();
        users.setChatId(chatId);
        usersList.add(users);
        return users;
    }


}

