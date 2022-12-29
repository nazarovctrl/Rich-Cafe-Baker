package com.example.admin.controller;

import com.example.admin.repository.AdminRepository;
import com.example.admin.repository.AdminMealsRepository;
import com.example.admin.repository.AdminMenuRepository;
import com.example.admin.repository.SupplierRepostoriy;
import com.example.admin.service.AdminMealsService;
import com.example.admin.service.MenuService;
import com.example.admin.service.SettingsService;
import com.example.admin.service.SupplierService;
import com.example.admin.util.MenuButtonUtil;
import com.example.entity.AdminEntity;
import com.example.entity.MealEntity;
import com.example.entity.MenuEntity;
import com.example.entity.OrdersEntity;
import com.example.enums.OrdersStatus;
import com.example.enums.Step;
import com.example.enums.UserRole;
import com.example.interfaces.Constant;
import com.example.service.OrdersService;
import com.example.step.TelegramUsers;
import com.example.utill.SendMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminMainController {

    private List<TelegramUsers> usersList = new LinkedList<>();
    @Autowired
    private MenuButtonUtil menuButtonUtil;
    @Autowired
    private MenuService menuService;
    @Autowired
    private AdminMenuRepository menuRepostoriy;
    @Autowired
    private AdminMealsService mealsService;
    @Autowired
    private AdminMealsRepository mealsRepository;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private AdminRepository adminRepostoriy;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private SupplierRepostoriy supplierRepostoriy;

    @Autowired
    private OrdersService ordersService;
    private MealEntity mealEntity = new MealEntity();
    private MenuEntity menuEntity = new MenuEntity();
    private AdminEntity adminEntity = new AdminEntity();

    public void handle(Message message) {

        TelegramUsers user = saveUser(message.getChatId());

        if (message.hasText() && message.getText().equals("/start") && user.getStep() == null) {
            menuButtonUtil.mainMenu(message);
            user.setStep(Step.MAIN);
        }

        if (message.hasText()) {

            switch (message.getText()) {

                case Constant.addMenu -> {

                    menuService.menuName(message);
                    user.setStep(Step.ADD_MENU_NAME);
                    return;
                }
                case Constant.attachMealToMenu -> {

                    mealsService.mealsName(message);
                    user.setStep(Step.ADD_MEALS_NAME);
                    return;
                }
                case Constant.deleteMeal -> {

                    boolean result = mealsService.menuList(message);

                    if (result) {
                        mealsService.deleteMeals1(message);
                        user.setStep(Step.MEALS_DELETE);
                        return;
                    }

                }
                case Constant.editePrice -> {

                    mealsService.menuList(message);
                    mealsService.updateMealsPrice1(message);
                    user.setStep(Step.MEALS_UPDATE_PRICE);
                    return;

                }
                case Constant.deleteMenu -> {

                    mealsService.menuList(message);
                    menuService.deleteMeals1(message);
                    user.setStep(Step.DELETE_MENU);
                    return;

                }
                case Constant.settings -> {

                    menuButtonUtil.settingMenu(message);
                    return;

                }
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

                }
                case Constant.listOfAdmin -> {

                    settingsService.adminListMessage(message);
                    user.setStep(Step.ADMIN_LIST);

                }
                case Constant.mealsList -> {

                    boolean result = mealsService.menuList(message);

                    if (result) {
                        mealsService.mealsListMessage(message);
                        user.setStep(Step.MEALS_LIST);
                    }

                }
                case Constant.menuList -> {

                    boolean result = mealsService.menuList(message);

                    if (result) {
                        mealsService.menuListGotovo(message);
                    }

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
                    user.setStep(Step.MAIN);
                    return;

                }
                case Constant.backSettingsPanel -> {
                    menuButtonUtil.settingMenu(message);
                    user.setStep(Step.MAIN);
                    return;

                }

            }
        }

        switch (user.getStep()) {

            case ADD_MENU_NAME -> {

                boolean checkMenuNameDataBase = menuService.checkMenuName(message);

                if (checkMenuNameDataBase) {
                    return;
                }

                menuEntity.setName(message.getText());
                menuRepostoriy.save(menuEntity);
                menuService.saveMenu(message);
                user.setStep(Step.MAIN);
                menuEntity = new MenuEntity();

            }
            case ADD_MEALS_NAME -> {

                boolean checkMealsName = mealsService.checkMealsName(message);

                if (checkMealsName) {
                    mealEntity.setName(message.getText());
                    mealsService.mealsPrice(message);
                    user.setStep(Step.ADD_MEALS_PRICE);
                }

            }
            case ADD_MEALS_PRICE -> {

                mealEntity.setPrice(Double.valueOf(message.getText()));
                mealsService.addPhoto(message);
//                mealsService.addMealsMenu(message);
                user.setStep(Step.ADD_MEALS_PHOTO);

            }


            case MEALS_MENU_lIST -> {

                Optional<MenuEntity> menuId = mealsService.returnetMenuId(message);
                MenuEntity entity = menuId.get();
                mealEntity.setMenu(entity);
                mealsRepository.save(mealEntity);
                menuService.saveMenu(message);
                user.setStep(Step.MAIN);
                mealEntity = new MealEntity();

            }
            case MEALS_DELETE -> {

                boolean result = mealsService.mealesList(message);

                if (result) {
                    mealsService.deleteMeal2(message);
                    user.setStep(Step.MEALS_DELETE_ID);
                }

            }
            case MEALS_DELETE_ID -> {

                boolean delete = mealsService.deleteMeal3(message);

                if (delete) {
                    mealsService.saveMenu(message);
                    user.setStep(Step.MAIN);
                }

            }
            case MEALS_UPDATE_PRICE -> {

                boolean result = mealsService.mealesListUpdate(message);

                if (result) {
                    mealsService.updateMeals(message);
                    user.setStep(Step.MEALS_UPDATE_BY_ID);
                }

            }
            case MEALS_UPDATE_BY_ID -> {

                Optional<MealEntity> mealEntityOptional = mealsService.updateMealsById(message);

                if (mealEntityOptional.isEmpty()) {
                    mealsService.nullMealsEntity(message);
                    return;
                }

                mealEntity = mealEntityOptional.get();
                mealsService.MealsByIdList(message, mealEntity);
                mealsService.addPrice(message);
                user.setStep(Step.UPDATE_PRICE);

            }

            case UPDATE_PRICE -> {

                mealEntity.setPrice(Double.valueOf(message.getText()));
                mealsRepository.save(mealEntity);
                mealsService.updatePriceMessage(message);
                user.setStep(Step.MAIN);

            }
            case DELETE_MENU -> {

                boolean delete = menuService.deleteMeal3(message);

                if (delete) {
                    menuService.saveMenuDelete(message);
                    user.setStep(Step.MAIN);
                }

            }
            case ADMIN_FULL_NAME -> {

                adminEntity.setFullname(message.getText());
                settingsService.addAdminPassword(message);
                user.setStep(Step.ADMIN_PASSWORD);

            }
            case ADMIN_PASSWORD -> {

                boolean checkpasswordDataBase = settingsService.checkpasswordDataBase(message);
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
                    adminEntity.setUserId(message.getChatId());
                    adminEntity.setRole(UserRole.ADMIN);
                    adminRepostoriy.save(adminEntity);
                    adminEntity = new AdminEntity();
                    settingsService.settingsMenu(message);
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
            case MEALS_LIST -> {

                boolean result = mealsService.mealsListAdmin(message);

                if (result) {
                    mealsService.melasListGotovo(message);
                    user.setStep(Step.MAIN);
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

                boolean checkPhoneDataBase = supplierService.chackPhoneDataBase(message);
                boolean checkPhone = supplierService.checkPhpone(message);

                if (checkPhoneDataBase) {
                    return;
                }

                if (checkPhone) {
                    adminEntity.setPhone(message.getText());
                    adminEntity.setUserId(message.getChatId());
                    adminEntity.setRole(UserRole.SUPPLIER);
                    supplierRepostoriy.save(adminEntity);
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

        }

        if (message.hasPhoto() && user.getStep().equals(Step.ADD_MEALS_PHOTO)) {
            mealEntity.setPhoto(message.getPhoto().get(0).getFileId());
            mealsService.menuList(message);
            mealsService.addMealsMenu(message);
            user.setStep(Step.MEALS_MENU_lIST);
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
