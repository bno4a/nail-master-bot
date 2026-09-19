package com.kazancev.nailbot.service;

import com.kazancev.nailbot.config.SalonProperties;
import com.kazancev.nailbot.entity.Appointment;
import com.kazancev.nailbot.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    private SlotService slotService;

    @BeforeEach
    void setUp() {
        SalonProperties properties = new SalonProperties(
                LocalTime.of(10, 0),
                LocalTime.of(19, 0),
                Duration.ofHours(2),
                DayOfWeek.SUNDAY,
                7
        );
        slotService = new SlotService(properties, appointmentRepository);
    }

    @Test
    void getAllSlots_returnsSlotsThatFitIntoWorkingDay() {
        List<LocalTime> slots = slotService.getAllSlots();

        assertThat(slots).containsExactly(
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                LocalTime.of(14, 0),
                LocalTime.of(16, 0)
        );
    }

    @Test
    void getFreeSlots_excludesBookedTime() {
        LocalDate date = LocalDate.of(2026, 9, 21);
        Appointment booked = new Appointment();
        booked.setDate(date);
        booked.setTime(LocalTime.of(12, 0));
        when(appointmentRepository.findByDateOrderByTimeAsc(date)).thenReturn(List.of(booked));

        List<LocalTime> freeSlots = slotService.getFreeSlots(date);

        assertThat(freeSlots).containsExactly(
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                LocalTime.of(16, 0)
        );
    }

    @Test
    void getBookingDates_startsFromTomorrowAndSkipsDayOff() {
        LocalDate saturday = LocalDate.of(2026, 9, 19);

        List<LocalDate> dates = slotService.getBookingDates(saturday);

        assertThat(dates).hasSize(6);
        assertThat(dates.get(0)).isEqualTo(LocalDate.of(2026, 9, 21));
        assertThat(dates).noneMatch(date -> date.getDayOfWeek() == DayOfWeek.SUNDAY);
    }
}
