package com.kazancev.nailbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

@ConfigurationProperties(prefix = "salon")
public record SalonProperties(
        LocalTime workStart,
        LocalTime workEnd,
        Duration slotDuration,
        DayOfWeek dayOff,
        int bookingDays
) {
}
