package com.kazancev.nailbot.service;

import com.kazancev.nailbot.entity.Appointment;
import com.kazancev.nailbot.entity.Client;
import com.kazancev.nailbot.entity.ServiceItem;
import com.kazancev.nailbot.repository.AppointmentRepository;
import com.kazancev.nailbot.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final ClientService clientService;

    public List<ServiceItem> getServices() {
        return serviceItemRepository.findAllByOrderByIdAsc();
    }

    public ServiceItem getService(Long serviceId) {
        return serviceItemRepository.findById(serviceId).orElseThrow();
    }

    public Appointment create(Long telegramId, String firstName, Long serviceId, LocalDate date, LocalTime time) {
        Client client = clientService.getOrCreate(telegramId, firstName);
        ServiceItem service = serviceItemRepository.findById(serviceId).orElseThrow();

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setService(service);
        appointment.setDate(date);
        appointment.setTime(time);

        try {
            return appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException e) {
            throw new SlotTakenException();
        }
    }

    public Optional<Appointment> cancel(Long appointmentId, Long telegramId) {
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId)
                .filter(a -> a.getClient().getTelegramId().equals(telegramId));
        appointment.ifPresent(appointmentRepository::delete);
        return appointment;
    }

    public List<Appointment> getUpcoming(Long telegramId, LocalDate today) {
        return appointmentRepository.findByClientTelegramIdAndDateGreaterThanEqualOrderByDateAscTimeAsc(telegramId, today);
    }

    public List<Appointment> getByDate(LocalDate date) {
        return appointmentRepository.findByDateOrderByTimeAsc(date);
    }
}
