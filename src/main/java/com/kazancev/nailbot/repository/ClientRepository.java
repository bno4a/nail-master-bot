package com.kazancev.nailbot.repository;

import com.kazancev.nailbot.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByTelegramId(Long telegramId);
}
