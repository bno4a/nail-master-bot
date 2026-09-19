package com.kazancev.nailbot.repository;

import com.kazancev.nailbot.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDateOrderByTimeAsc(LocalDate date);

    List<Appointment> findByClientTelegramIdAndDateGreaterThanEqualOrderByDateAscTimeAsc(Long telegramId, LocalDate from);
}
