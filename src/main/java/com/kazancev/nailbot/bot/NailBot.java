package com.kazancev.nailbot.bot;

import com.kazancev.nailbot.entity.ServiceItem;
import com.kazancev.nailbot.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class NailBot extends TelegramLongPollingBot {

    private final String username;
    private final AppointmentService appointmentService;

    public NailBot(@Value("${bot.token}") String token,
                   @Value("${bot.username}") String username,
                   AppointmentService appointmentService) {
        super(token);
        this.username = username;
        this.appointmentService = appointmentService;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        Message message = update.getMessage();
        Long chatId = message.getChatId();

        switch (message.getText()) {
            case "/start" -> send(chatId, greeting(message.getFrom().getFirstName()));
            case Keyboards.PRICE -> send(chatId, priceList());
            case Keyboards.BOOK, Keyboards.MY_APPOINTMENTS -> send(chatId, "Этот раздел скоро заработает.");
            default -> send(chatId, "Не понимаю. Выберите пункт меню.");
        }
    }

    private String greeting(String firstName) {
        return "Привет, " + firstName + "!\n"
                + "Здесь можно записаться на маникюр. Выберите действие в меню.";
    }

    private String priceList() {
        StringBuilder text = new StringBuilder("Прайс:\n");
        for (ServiceItem service : appointmentService.getServices()) {
            text.append("\n").append(service.getName()).append(" — ").append(service.getPrice()).append(" ₽");
        }
        return text.toString();
    }

    private void send(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(Keyboards.mainMenu())
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в чат {}", chatId, e);
        }
    }
}
