package com.kazancev.nailbot.service;

import com.kazancev.nailbot.config.SalonProperties;
import com.kazancev.nailbot.entity.Appointment;
import com.kazancev.nailbot.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SalonProperties properties;
    private final AppointmentRepository appointmentRepository;

    public List<LocalTime> getAllSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime time = properties.workStart();
        while (!time.plus(properties.slotDuration()).isAfter(properties.workEnd())) {
            slots.add(time);
            time = time.plus(properties.slotDuration());
        }
        return slots;
    }

    public List<LocalDate> getBookingDates(LocalDate today) {
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 1; i <= properties.bookingDays(); i++) {
            LocalDate date = today.plusDays(i);
            if (date.getDayOfWeek() != properties.dayOff()) {
                dates.add(date);
            }
        }
        return dates;
    }

    public List<LocalTime> getFreeSlots(LocalDate date) {
        List<LocalTime> busy = appointmentRepository.findByDateOrderByTimeAsc(date).stream()
                .map(Appointment::getTime)
                .toList();
        return getAllSlots().stream()
                .filter(time -> !busy.contains(time))
                .toList();
    }
}
