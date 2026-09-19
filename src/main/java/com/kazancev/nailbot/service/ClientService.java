package com.kazancev.nailbot.service;

import com.kazancev.nailbot.entity.Client;
import com.kazancev.nailbot.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public Client getOrCreate(Long telegramId, String firstName) {
        return clientRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    Client client = new Client();
                    client.setTelegramId(telegramId);
                    client.setFirstName(firstName);
                    return clientRepository.save(client);
                });
    }
}
