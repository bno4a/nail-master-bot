package com.kazancev.nailbot.bot;

import com.kazancev.nailbot.entity.ServiceItem;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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

    public static InlineKeyboardMarkup services(List<ServiceItem> services) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (ServiceItem service : services) {
            String text = service.getName() + " — " + service.getPrice() + " ₽";
            rows.add(List.of(button(text, "service:" + service.getId())));
        }
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public static InlineKeyboardMarkup dates(List<LocalDate> dates) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (LocalDate date : dates) {
            rows.add(List.of(button(Formats.DATE.format(date), "date:" + date)));
        }
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public static InlineKeyboardMarkup times(List<LocalTime> times) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (LocalTime time : times) {
            rows.add(List.of(button(Formats.TIME.format(time), "time:" + time)));
        }
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public static InlineKeyboardMarkup confirmation() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button("Подтвердить", "confirm"),
                        button("Отмена", "cancel_booking")
                ))
                .build();
    }

    public static InlineKeyboardMarkup cancelAppointment(Long appointmentId) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("Отменить", "cancel:" + appointmentId)))
                .build();
    }

    private static InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
