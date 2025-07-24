package ru.taranenko.personal_assist_bot.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

/**
 * @author Dmitrii Taranenko
 */
public class Controls {

    public static ReplyKeyboardMarkup createDefaultKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Услуги"));
        row1.add(new KeyboardButton("Оставить заявку"));

        keyboardMarkup.setKeyboard(List.of(row1));

        return keyboardMarkup;
    }
}
