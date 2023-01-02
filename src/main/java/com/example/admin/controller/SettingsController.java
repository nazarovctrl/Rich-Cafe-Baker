package com.example.admin.controller;

import com.example.admin.repository.AdminRepository;
import com.example.admin.service.SettingsService;
import com.example.admin.service.SupplierService;
import com.example.admin.util.MenuButtonUtil;
import com.example.entity.AdminEntity;
import com.example.enums.Step;
import com.example.enums.UserRole;
import com.example.interfaces.Constant;
import com.example.myTelegramBot.MyTelegramBot;
import com.example.step.TelegramUsers;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SettingsController {

    private List<TelegramUsers> usersList = new ArrayList<>();

    private final MenuButtonUtil menuButtonUtil;

    private final SupplierService supplierService;

    private final SettingsService settingsService;
    private final AdminRepository adminRepository;

    private final MyTelegramBot myTelegramBot;

    private final CookerMenuController menuController;

    private final AdminMainController adminMainController;

    private AdminEntity adminEntity = new AdminEntity();


    public SettingsController(MenuButtonUtil menuButtonUtil, SupplierService supplierService, SettingsService settingsService, AdminRepository adminRepository, MyTelegramBot myTelegramBot, CookerMenuController menuController, AdminMainController adminMainController) {
        this.menuButtonUtil = menuButtonUtil;
        this.supplierService = supplierService;
        this.settingsService = settingsService;
        this.adminRepository = adminRepository;
        this.myTelegramBot = myTelegramBot;
        this.menuController = menuController;
        this.adminMainController = adminMainController;
    }

    public void handle(Message message) {

        TelegramUsers user = saveUser(message.getChatId());
        TelegramUsers step = adminMainController.saveUser(message.getChatId());

        if (message.hasText()) {
            String text = message.getText();


            switch (text) {
                case Constant.addAdmin -> {

                    settingsService.addAdminFullName(message);
                    user.setStep(Step.ADMIN_FULL_NAME);
                    return;

                }
                case Constant.deleteAdmin -> {

                    boolean result = settingsService.adminList(message);

                    if (result) {
                        settingsService.deleteAdmin(message);
                        user.setStep(Step.DELETE_ADMIN);
                    }
                    return;

                }
                case Constant.listOfAdmin -> {

                    settingsService.adminListMessage(message);
                    user.setStep(Step.ADMIN_LIST);

                }
                case Constant.addSupplier -> {

                    supplierService.addSuplierFullName(message);
                    user.setStep(Step.SUPPLIER_NAME);
                    return;

                }
                case Constant.deleteSupplier -> {

                    boolean result = supplierService.supplierList(message);

                    if (result) {
                        supplierService.deleteSupplierMessage(message);
                        user.setStep(Step.DELETE_SUPPLIER);
                        return;
                    }

                }
                case Constant.supplierList -> {

                    supplierService.supplierListMessage(message);
                    user.setStep(Step.SUPPLIER_LIST);

                }
                case Constant.backMenu -> {
                    menuButtonUtil.mainMenu(message);
                    step.setStep(Step.MAIN);
                    return;

                }
                case Constant.backSettingsPanel -> {
                    menuButtonUtil.settingMenu(message);
                    user.setStep(null);
                    return;

                }

                case Constant.addCooker -> {
                    menuController.nameCooker(message);
                    user.setStep(Step.COOKER_NAME);
                    return;
                }

                case Constant.deleteCooker -> {
                    boolean result = menuController.cookerList(message);

                    if (result) {
                        menuController.deleteCookerMessage(message);
                        user.setStep(Step.DELETE_COOKER);
                        return;
                    }

                }
                case Constant.listCooker -> {
                    menuController.cookerList(message);
                }
            }
            if (user.getStep() != null) {
                switch (user.getStep()) {
                    case ADMIN_FULL_NAME -> {

                        adminEntity.setFullname(message.getText());
                        settingsService.addAdminPassword(message);
                        user.setStep(Step.ADMIN_PASSWORD);

                    }
                    case ADMIN_PASSWORD -> {

                        boolean checkpasswordDataBase = settingsService.checkPasswordDataBase(message);
                        boolean checkpassword = settingsService.checkpassword(message);

                        if (checkpasswordDataBase) {
                            return;
                        }

                        if (checkpassword) {
                            adminEntity.setPassword(message.getText());
                            settingsService.addAdminPhone(message);
                            user.setStep(Step.ADMIN_PHONE);
                        }

                    }
                    case ADMIN_PHONE -> {

                        boolean phonecheckDataBase = settingsService.checkPhoneDataBase(message);
                        boolean phonecheck = settingsService.checkPhoneNumber(message);

                        if (phonecheckDataBase) {
                            return;
                        }

                        if (phonecheck) {
                            adminEntity.setPhone(message.getText());
                            adminEntity.setUserId(null);
                            adminEntity.setRole(UserRole.ADMIN);
                            adminRepository.save(adminEntity);
                            adminEntity = new AdminEntity();
                            settingsService.settingsMenuAdmin(message);
                            user.setStep(Step.MAIN);
                        }

                    }
                    case DELETE_ADMIN -> {

                        boolean result = settingsService.deleteAdminById(message);

                        if (result) {
                            settingsService.deleteAdminGotovo(message);
                            user.setStep(Step.MAIN);
                        }

                    }
                    case ADMIN_LIST -> {

                        boolean result = settingsService.adminList(message);

                        if (result) {
                            settingsService.adminListGotovo(message);
                        }

                    }

                    case SUPPLIER_NAME -> {

                        adminEntity.setFullname(message.getText());
                        supplierService.addSupplierPassword(message);
                        user.setStep(Step.SUPPLIER_PASSWORD);

                    }
                    case SUPPLIER_PASSWORD -> {

                        boolean checkPasswordDataBase = supplierService.checkPasswordDataBase(message);
                        boolean checkPassword = supplierService.checkPassword(message);

                        if (checkPasswordDataBase) {
                            return;
                        }

                        if (checkPassword) {
                            supplierService.addSupplierPhone(message);
                            adminEntity.setPassword(message.getText());
                            user.setStep(Step.SUPPLIER_PHONE);
                        }

                    }
                    case SUPPLIER_PHONE -> {

                        boolean checkPhoneDataBase = supplierService.checkPhoneDataBase(message);
                        boolean checkPhone = supplierService.checkPhpone(message);

                        if (checkPhoneDataBase) {
                            return;
                        }

                        if (checkPhone) {
                            adminEntity.setPhone(message.getText());
                            adminEntity.setUserId(null);
                            adminEntity.setRole(UserRole.SUPPLIER);
                            adminRepository.save(adminEntity);
                            adminEntity = new AdminEntity();
                            supplierService.settingsMenu(message);
                            user.setStep(Step.MAIN);
                        }

                    }
                    case DELETE_SUPPLIER -> {

                        boolean delete = supplierService.deleteSupplierById(message);

                        if (delete) {
                            supplierService.deleteSupplierGotovo(message);
                            user.setStep(Step.MAIN);
                        }

                    }
                    case SUPPLIER_LIST -> {

                        boolean result = supplierService.supplierList(message);

                        if (result) {
                            supplierService.supplierListGotovo(message);
                            user.setStep(Step.MAIN);
                        }
                    }
                    case COOKER_NAME -> {

                        adminEntity.setFullname(message.getText());
                        menuController.addPasswordCooker(message);
                        user.setStep(Step.COOKER_PASSWORD);
                    }
                    case COOKER_PASSWORD -> {
                        boolean checkPasswordDataBase = settingsService.checkPasswordDataBase(message);
                        boolean checkPassword = menuController.checkPassword(message);

                        if (checkPasswordDataBase) return;

                        if (checkPassword) {
                            adminEntity.setPassword(message.getText());
                            menuController.addCookerPhone(message);
                            user.setStep(Step.COOKER_PHONE);
                        }
                    }

                    case COOKER_PHONE -> {
                        boolean phoneCheckDataBase = settingsService.checkPhoneDataBase(message);
                        boolean phoneCheck = menuController.checkPhone(message);

                        if (phoneCheckDataBase) {
                            return;
                        }

                        if (phoneCheck) {
                            adminEntity.setPhone(message.getText());
                            adminEntity.setUserId(null);
                            adminEntity.setRole(UserRole.COOKER);
                            adminRepository.save(adminEntity);
                            adminEntity = new AdminEntity();
                            settingsService.settingsMenuCooker(message);
                            user.setStep(Step.MAIN);
                            return;
                        }
                    }
                    case DELETE_COOKER -> {

                        boolean delete = menuController.deleteCookerById(message);

                        if (delete) {
                            menuController.deletedCooker(message);
                            user.setStep(Step.MAIN);
                        }

                    }
                }
            }
            return;
        }
        user.setStep(null);
        handle(message);

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
