package com.example.utill;

import com.example.entity.MealEntity;
import com.example.entity.MenuEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

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
}
