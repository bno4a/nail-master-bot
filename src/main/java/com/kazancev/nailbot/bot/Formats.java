package com.kazancev.nailbot.bot;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formats {

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM, EEE", Locale.forLanguageTag("ru"));
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private Formats() {
    }
}
