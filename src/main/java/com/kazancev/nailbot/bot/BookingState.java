package com.kazancev.nailbot.bot;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class BookingState {

    private Long serviceId;
    private LocalDate date;
    private LocalTime time;
}
