package com.kazancev.nailbot.service;

import com.kazancev.nailbot.repository.AppointmentRepository;
import com.kazancev.nailbot.repository.ClientRepository;
import com.kazancev.nailbot.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class BookingConcurrencyTest extends IntegrationTest {

    private static final int CLIENTS = 8;
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

    @AfterEach
    void tearDown() {
        appointmentRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void create_letsOnlyOneOfSeveralClientsTakeTheSameSlot() throws Exception {
        Long serviceId = appointmentService.getServices().get(0).getId();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(CLIENTS);
        List<Future<Boolean>> attempts = new ArrayList<>();

        for (int i = 0; i < CLIENTS; i++) {
            long telegramId = 1000 + i;
            attempts.add(pool.submit(() -> {
                start.await();
                try {
                    appointmentService.create(telegramId, "Client " + telegramId, serviceId, TOMORROW, TEN);
                    return true;
                } catch (SlotTakenException e) {
                    return false;
                }
            }));
        }

        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        long booked = 0;
        for (Future<Boolean> attempt : attempts) {
            if (attempt.get()) {
                booked++;
            }
        }

        assertThat(booked).isEqualTo(1);
        assertThat(appointmentRepository.findByDateOrderByTimeAsc(TOMORROW)).hasSize(1);
        assertThat(slotService.getFreeSlots(TOMORROW)).doesNotContain(TEN);
    }
}
