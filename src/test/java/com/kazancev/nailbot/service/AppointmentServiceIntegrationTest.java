package com.kazancev.nailbot.service;

import com.kazancev.nailbot.entity.Appointment;
import com.kazancev.nailbot.entity.ServiceItem;
import com.kazancev.nailbot.repository.AppointmentRepository;
import com.kazancev.nailbot.repository.ClientRepository;
import com.kazancev.nailbot.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class AppointmentServiceIntegrationTest extends IntegrationTest {

    private static final Long ANNA = 111L;
    private static final Long OLGA = 222L;
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private SlotService slotService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Long serviceId;

    @BeforeEach
    void setUp() {
        serviceId = appointmentService.getServices().get(0).getId();
    }

    @Test
    void getServices_returnsPriceListFromMigration() {
        assertThat(appointmentService.getServices())
                .extracting(ServiceItem::getName)
                .containsExactly("Маникюр", "Маникюр с покрытием", "Снятие покрытия");
    }

    @Test
    void create_savesAppointmentThatAppearsInUpcoming() {
        appointmentService.create(ANNA, "Anna", serviceId, TOMORROW, TEN);

        assertThat(appointmentService.getUpcoming(ANNA, LocalDate.now()))
                .singleElement()
                .satisfies(appointment -> {
                    assertThat(appointment.getDate()).isEqualTo(TOMORROW);
                    assertThat(appointment.getTime()).isEqualTo(TEN);
                    assertThat(appointment.getClient().getTelegramId()).isEqualTo(ANNA);
                    assertThat(appointment.getService().getId()).isEqualTo(serviceId);
                });
    }

    @Test
    void create_throwsSlotTakenException_whenTheSameSlotIsBookedTwice() {
        appointmentService.create(ANNA, "Anna", serviceId, TOMORROW, TEN);

        assertThatThrownBy(() -> appointmentService.create(OLGA, "Olga", serviceId, TOMORROW, TEN))
                .isInstanceOf(SlotTakenException.class);
    }

    @Test
    void create_allowsTheSameTimeOnAnotherDate() {
        appointmentService.create(ANNA, "Anna", serviceId, TOMORROW, TEN);
        appointmentService.create(OLGA, "Olga", serviceId, TOMORROW.plusDays(1), TEN);

        assertThat(appointmentRepository.findAll()).hasSize(2);
    }

    @Test
    void create_reusesExistingClient() {
        appointmentService.create(ANNA, "Anna", serviceId, TOMORROW, TEN);
        appointmentService.create(ANNA, "Anna", serviceId, TOMORROW, LocalTime.of(12, 0));

        assertThat(clientRepository.findAll()).hasSize(1);
    }

    @Test
    void getUpcoming_returnsOnlyOwnAppointmentsSortedByDateAndTime() {
        appointmentService.create(ANNA, "Anna", serviceId, TOMORROW.plusDays(1), TEN);
        appointmentService.create(ANNA, "Anna", serviceId, TOMORROW, LocalTime.of(12, 0));
        appointmentService.create(ANNA, "Anna", serviceId, TOMORROW, TEN);
        appointmentService.create(OLGA, "Olga", serviceId, TOMORROW, LocalTime.of(14, 0));

        List<Appointment> upcoming = appointmentService.getUpcoming(ANNA, LocalDate.now());

        assertThat(upcoming).extracting(Appointment::getDate, Appointment::getTime).containsExactly(
                tuple(TOMORROW, TEN),
                tuple(TOMORROW, LocalTime.of(12, 0)),
                tuple(TOMORROW.plusDays(1), TEN));
    }

    @Test
    void cancel_removesAppointmentFromDatabase() {
        Appointment appointment = appointmentService.create(ANNA, "Anna", serviceId, TOMORROW, TEN);

        assertThat(appointmentService.cancel(appointment.getId(), ANNA)).isPresent();

        assertThat(appointmentRepository.findByDateOrderByTimeAsc(TOMORROW)).isEmpty();
    }

    @Test
    void cancel_keepsAppointmentOfAnotherClient() {
        Appointment appointment = appointmentService.create(ANNA, "Anna", serviceId, TOMORROW, TEN);

        assertThat(appointmentService.cancel(appointment.getId(), OLGA)).isEmpty();

        assertThat(appointmentRepository.findByDateOrderByTimeAsc(TOMORROW)).hasSize(1);
    }

    @Test
    void getFreeSlots_excludesBookedTime() {
        appointmentService.create(ANNA, "Anna", serviceId, TOMORROW, TEN);

        assertThat(slotService.getFreeSlots(TOMORROW)).doesNotContain(TEN);
    }
}
