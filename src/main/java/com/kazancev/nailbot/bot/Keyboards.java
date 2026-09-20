package com.kazancev.nailbot.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public final class Keyboards {

    public static final String BOOK = "Записаться";
    public static final String MY_APPOINTMENTS = "Мои записи";
    public static final String PRICE = "Прайс";

    private Keyboards() {
    }

    public static ReplyKeyboardMarkup mainMenu() {
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(BOOK);

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(MY_APPOINTMENTS);
        secondRow.add(PRICE);

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(firstRow, secondRow))
                .resizeKeyboard(true)
                .build();
    }
}
