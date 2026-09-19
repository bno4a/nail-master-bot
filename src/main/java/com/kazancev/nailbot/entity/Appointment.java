package com.kazancev.nailbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "appointments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_appointments_date_time",
                columnNames = {"appointment_date", "appointment_time"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id")
    private ServiceItem service;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate date;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime time;
}
