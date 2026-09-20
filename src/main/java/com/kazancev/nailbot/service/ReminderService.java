package com.kazancev.nailbot.service;

import com.kazancev.nailbot.bot.NailBot;
import com.kazancev.nailbot.entity.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final AppointmentService appointmentService;
    private final NailBot nailBot;

    @Scheduled(cron = "${bot.reminder-cron}")
    public void sendReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> appointments = appointmentService.getByDate(tomorrow);
        for (Appointment appointment : appointments) {
            nailBot.sendReminder(appointment);
        }
        log.info("Напоминаний на {} отправлено: {}", tomorrow, appointments.size());
    }
}
