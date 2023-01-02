package com.example.admin.controller;

import com.example.admin.repository.AdminMealsRepository;
import com.example.admin.repository.AdminMenuRepository;
import com.example.admin.service.AdminMealsService;
import com.example.admin.service.MenuService;
import com.example.admin.util.MenuButtonUtil;
import com.example.entity.AdminEntity;
import com.example.entity.MealEntity;
import com.example.entity.MenuEntity;
import com.example.enums.Step;
import com.example.interfaces.Constant;
import com.example.step.TelegramUsers;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminMainController {

    private List<TelegramUsers> usersList = new LinkedList<>();
    private final MenuButtonUtil menuButtonUtil;
    private final MenuService menuService;
    private final AdminMenuRepository menuRepository;
    private final AdminMealsService mealsService;
    private final AdminMealsRepository mealsRepository;

    private final SettingsController settingsController;
    private MealEntity mealEntity = new MealEntity();
    private MenuEntity menuEntity = new MenuEntity();
    private AdminEntity adminEntity = new AdminEntity();

    @Lazy
    public AdminMainController(MenuButtonUtil menuButtonUtil, MenuService menuService, AdminMenuRepository menuRepository, AdminMealsService mealsService, AdminMealsRepository mealsRepository, SettingsController settingsController) {
        this.menuButtonUtil = menuButtonUtil;
        this.menuService = menuService;
        this.menuRepository = menuRepository;
        this.mealsService = mealsService;
        this.mealsRepository = mealsRepository;
        this.settingsController = settingsController;
    }

    public void handle(Message message) {

        TelegramUsers user = saveUser(message.getChatId());
        TelegramUsers step = settingsController.saveUser(message.getChatId());

        if (message.hasText() && message.getText().equals("/start") || user.getStep() == null) {
            menuButtonUtil.mainMenu(message);
            user.setStep(Step.MAIN);
            step.setStep(null);
            return;

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
                    user.setStep(Step.SETTINGS);
                    return;
                }
//                case Constant.backSettingsPanel -> {
//                    menuButtonUtil.settingMenu(message);
//                    user.setStep(Step.SETTINGS);
//                    return;
//                }

                case Constant.mealsList -> {

                    boolean result = mealsService.menuList(message);

                    if (result) {
                        mealsService.mealsListMessage(message);
                        user.setStep(Step.MEALS_LIST);
                    }

                    return;
                }
                case Constant.menuList -> {

                    boolean result = mealsService.menuList(message);

                    if (result) {
                        mealsService.menuListGotovo(message);
                    }
                    return;
                }

                case Constant.backMenu -> {
                    menuButtonUtil.mainMenu(message);
                    user.setStep(Step.MAIN);
                    return;

                }


            }
        }

        if (user.getStep().equals(Step.SETTINGS)) {
            settingsController.handle(message);
            return;
        }

        switch (user.getStep()) {

            case ADD_MENU_NAME -> {

                boolean checkMenuNameDataBase = menuService.checkMenuName(message);

                if (checkMenuNameDataBase) {
                    return;
                }

                menuEntity.setName(message.getText());
                menuRepository.save(menuEntity);
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

                boolean result = mealsService.mealsListUpdate(message);

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

            case MEALS_LIST -> {

                boolean result = mealsService.mealsListAdmin(message);

                if (result) {
                    mealsService.melasListGotovo(message);
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
