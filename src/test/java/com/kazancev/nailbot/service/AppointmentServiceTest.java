package com.kazancev.nailbot.service;

import com.kazancev.nailbot.entity.Appointment;
import com.kazancev.nailbot.entity.Client;
import com.kazancev.nailbot.entity.ServiceItem;
import com.kazancev.nailbot.repository.AppointmentRepository;
import com.kazancev.nailbot.repository.ServiceItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final Long TELEGRAM_ID = 111L;
    private static final LocalDate DATE = LocalDate.of(2026, 9, 21);
    private static final LocalTime TIME = LocalTime.of(10, 0);

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceItemRepository serviceItemRepository;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void create_savesAppointmentWithClientServiceDateAndTime() {
        Client client = createClient(TELEGRAM_ID);
        ServiceItem service = new ServiceItem();
        when(clientService.getOrCreate(TELEGRAM_ID, "Anna")).thenReturn(client);
        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(service));
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.create(TELEGRAM_ID, "Anna", 1L, DATE, TIME);

        assertThat(result.getClient()).isEqualTo(client);
        assertThat(result.getService()).isEqualTo(service);
        assertThat(result.getDate()).isEqualTo(DATE);
        assertThat(result.getTime()).isEqualTo(TIME);
    }

    @Test
    void create_throwsSlotTakenException_whenTimeIsAlreadyBooked() {
        when(clientService.getOrCreate(TELEGRAM_ID, "Anna")).thenReturn(createClient(TELEGRAM_ID));
        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(new ServiceItem()));
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> appointmentService.create(TELEGRAM_ID, "Anna", 1L, DATE, TIME))
                .isInstanceOf(SlotTakenException.class);
    }

    @Test
    void cancel_deletesOwnAppointment() {
        Appointment appointment = createAppointment(TELEGRAM_ID);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));

        Optional<Appointment> result = appointmentService.cancel(5L, TELEGRAM_ID);

        assertThat(result).contains(appointment);
        verify(appointmentRepository).delete(appointment);
    }

    @Test
    void cancel_doesNotDeleteAppointmentOfAnotherClient() {
        Appointment appointment = createAppointment(222L);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));

        Optional<Appointment> result = appointmentService.cancel(5L, TELEGRAM_ID);

        assertThat(result).isEmpty();
        verify(appointmentRepository, never()).delete(any());
    }

    private Client createClient(Long telegramId) {
        Client client = new Client();
        client.setTelegramId(telegramId);
        return client;
    }

    private Appointment createAppointment(Long clientTelegramId) {
        Appointment appointment = new Appointment();
        appointment.setClient(createClient(clientTelegramId));
        appointment.setDate(DATE);
        appointment.setTime(TIME);
        return appointment;
    }
}
