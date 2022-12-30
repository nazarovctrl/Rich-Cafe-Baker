package com.example.utill;

import com.example.entity.MealEntity;
import com.example.entity.MenuEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Button {
    public static KeyboardButton button(String text) {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(text);
        return keyboardButton;
    }

    public static KeyboardButton button() {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setRequestContact(true);
        keyboardButton.setText("Share Contact");
        return keyboardButton;
    }

    public static KeyboardButton location() {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setRequestLocation(true);
        keyboardButton.setText("Manzilni jo'natish");
        return keyboardButton;
    }

    public static KeyboardRow row(KeyboardButton... keyboardButtons) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.addAll(Arrays.asList(keyboardButtons));
        return keyboardRow;
    }

    public static List<KeyboardRow> rowList(KeyboardRow... keyboardRows) {
        List<KeyboardRow> rows = new LinkedList<>();
        rows.addAll(Arrays.asList(keyboardRows));

        return rows;
    }

    public static ReplyKeyboardMarkup markup(List<KeyboardRow> rowList) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(rowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup markupMenu(List<MenuEntity> menuList, List<KeyboardRow> rows) {

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> rowList = new LinkedList<>();


        for (int i = 0; i < menuList.size(); i++) {
            KeyboardRow row = new KeyboardRow();
            for (int j = 0; j < 3; j++) {

                if (i >= menuList.size()) {
                    break;
                }
                row.add(button(menuList.get(i++).getName()));
            }

            i--;

            rowList.add(row);
        }
        rowList.addAll(rows);

        replyKeyboardMarkup.setKeyboard(rowList);
        replyKeyboardMarkup.setResizeKeyboard(true);


        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup buttonCount(List<KeyboardRow> rows) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> rowList = new LinkedList<>();


        for (int i = 1; i <= 9; i++) {
            KeyboardRow row = new KeyboardRow();
            for (int j = 1; j <= 3; j++) {

                row.add(button(String.valueOf(i++)));
            }
            i--;

            rowList.add(row);
        }
        rowList.addAll(rows);

        replyKeyboardMarkup.setKeyboard(rowList);
        replyKeyboardMarkup.setResizeKeyboard(true);


        return replyKeyboardMarkup;

    }

    public static ReplyKeyboardMarkup markupMeal(List<MealEntity> menuList, List<KeyboardRow> rows) {

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> rowList = new LinkedList<>();


        for (int i = 0; i < menuList.size(); i++) {
            KeyboardRow row = new KeyboardRow();
            for (int j = 0; j < 3; j++) {

                if (i >= menuList.size()) {
                    break;
                }
                row.add(button(menuList.get(i++).getName()));
            }

            i--;

            rowList.add(row);
        }
        rowList.addAll(rows);

        replyKeyboardMarkup.setKeyboard(rowList);
        replyKeyboardMarkup.setResizeKeyboard(true);


        return replyKeyboardMarkup;
    }

    public static List<InlineKeyboardButton> delivery(Integer oderId, Integer messageId) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("✅ Qabul qilish");
        button.setCallbackData("delivery/" + oderId + "/" + messageId);


        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        return row;
    }

    public static List<InlineKeyboardButton> locationForSupplier(Integer orderId, Integer messageId) {

        InlineKeyboardButton location = new InlineKeyboardButton();
        location.setText("\uD83D\uDCCD Yetkazib berish manzili");
        location.setCallbackData("locS/" + orderId + "/" + messageId);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(location);
        return row;
    }

    public static List<InlineKeyboardButton> locationForAdmin(Integer orderId, Integer messageId) {

        InlineKeyboardButton location = new InlineKeyboardButton();
        location.setText("\uD83D\uDCCD Yetkazib berish manzili");
        location.setCallbackData("locA/" + orderId + "/" + messageId);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(location);

        return row;
    }

    public static List<InlineKeyboardButton> location(Integer orderId, boolean isSupplier) {

        InlineKeyboardButton location = new InlineKeyboardButton();
        location.setText("\uD83D\uDCCD Yetkazib berish manzili");

        if (isSupplier) {
            location.setCallbackData("location/" + orderId+"/s");
        } else {
            location.setCallbackData("location/" + orderId+"/ ");
        }
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(location);

        return row;
    }

    public static InlineKeyboardButton save(Long userId, Integer orderId, Integer messageId) {

        InlineKeyboardButton save = new InlineKeyboardButton();
        save.setText("✅ Qabul qilish");
        save.setCallbackData("save/" + userId + "/" + orderId + "/" + messageId);
        return save;
    }

    public static InlineKeyboardButton cancel(Long userId, Integer orderId, Integer messageId) {

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("❌ Bekor qilish");
        cancel.setCallbackData("cancel/" + userId + "/" + orderId + "/" + messageId);
        return cancel;
    }

    public static List<InlineKeyboardButton> finish(Integer orderId) {
        InlineKeyboardButton save = new InlineKeyboardButton();
        save.setText("Yakunlash");
        save.setCallbackData("finish/" + orderId);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(save);
        return row;
    }

    public static InlineKeyboardButton cancelForSupplier(Integer orderId) {
        InlineKeyboardButton radEtish = new InlineKeyboardButton();
        radEtish.setText("❌ Rad etish");
        radEtish.setCallbackData("cancelS/" + orderId);
        return radEtish;
    }
}
