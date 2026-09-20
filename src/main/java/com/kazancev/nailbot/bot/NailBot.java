package com.kazancev.nailbot.bot;

import com.kazancev.nailbot.entity.Appointment;
import com.kazancev.nailbot.entity.ServiceItem;
import com.kazancev.nailbot.service.AppointmentService;
import com.kazancev.nailbot.service.SlotService;
import com.kazancev.nailbot.service.SlotTakenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NailBot extends TelegramLongPollingBot {

    private static final String RESTART = "Кнопка устарела. Нажмите «Записаться», чтобы начать заново.";

    private final String username;
    private final AppointmentService appointmentService;
    private final SlotService slotService;
    private final Map<Long, BookingState> bookingStates = new ConcurrentHashMap<>();

    public NailBot(@Value("${bot.token}") String token,
                   @Value("${bot.username}") String username,
                   AppointmentService appointmentService,
                   SlotService slotService) {
        super(token);
        this.username = username;
        this.appointmentService = appointmentService;
        this.slotService = slotService;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update.getMessage());
        }
    }

    private void handleMessage(Message message) {
        Long chatId = message.getChatId();
        Long telegramId = message.getFrom().getId();

        switch (message.getText()) {
            case "/start" -> send(chatId, greeting(message.getFrom().getFirstName()));
            case Keyboards.BOOK -> startBooking(chatId, telegramId);
            case Keyboards.MY_APPOINTMENTS -> showMyAppointments(chatId, telegramId);
            case Keyboards.PRICE -> send(chatId, priceList());
            default -> send(chatId, "Не понимаю. Выберите пункт меню.");
        }
    }

    private void handleCallback(CallbackQuery callback) {
        Long chatId = callback.getMessage().getChatId();
        User from = callback.getFrom();
        String[] parts = callback.getData().split(":", 2);
        String command = parts[0];
        String value = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "service" -> chooseDate(chatId, from.getId(), Long.parseLong(value));
            case "date" -> chooseTime(chatId, from.getId(), LocalDate.parse(value));
            case "time" -> confirmBooking(chatId, from.getId(), LocalTime.parse(value));
            case "confirm" -> createAppointment(chatId, from);
            case "cancel_booking" -> stopBooking(chatId, from.getId());
            case "cancel" -> cancelAppointment(chatId, from.getId(), Long.parseLong(value));
            default -> send(chatId, RESTART);
        }
        answerCallback(callback.getId());
    }

    private void startBooking(Long chatId, Long telegramId) {
        bookingStates.put(telegramId, new BookingState());
        send(chatId, "Выберите услугу:", Keyboards.services(appointmentService.getServices()));
    }

    private void chooseDate(Long chatId, Long telegramId, Long serviceId) {
        BookingState state = bookingStates.get(telegramId);
        if (state == null) {
            send(chatId, RESTART);
            return;
        }
        state.setServiceId(serviceId);
        send(chatId, "Выберите дату:", Keyboards.dates(slotService.getBookingDates(LocalDate.now())));
    }

    private void chooseTime(Long chatId, Long telegramId, LocalDate date) {
        BookingState state = bookingStates.get(telegramId);
        if (state == null) {
            send(chatId, RESTART);
            return;
        }
        state.setDate(date);
        List<LocalTime> freeSlots = slotService.getFreeSlots(date);
        if (freeSlots.isEmpty()) {
            send(chatId, "На этот день всё занято. Выберите другую дату:",
                    Keyboards.dates(slotService.getBookingDates(LocalDate.now())));
            return;
        }
        send(chatId, "Выберите время:", Keyboards.times(freeSlots));
    }

    private void confirmBooking(Long chatId, Long telegramId, LocalTime time) {
        BookingState state = bookingStates.get(telegramId);
        if (state == null || state.getDate() == null) {
            send(chatId, RESTART);
            return;
        }
        state.setTime(time);
        ServiceItem service = appointmentService.getService(state.getServiceId());
        String text = "Проверьте запись:\n\n"
                + service.getName() + " — " + service.getPrice() + " ₽\n"
                + Formats.DATE.format(state.getDate()) + ", " + Formats.TIME.format(time);
        send(chatId, text, Keyboards.confirmation());
    }

    private void createAppointment(Long chatId, User from) {
        BookingState state = bookingStates.get(from.getId());
        if (state == null || state.getTime() == null) {
            send(chatId, RESTART);
            return;
        }
        try {
            Appointment appointment = appointmentService.create(
                    from.getId(), from.getFirstName(), state.getServiceId(), state.getDate(), state.getTime());
            bookingStates.remove(from.getId());
            send(chatId, "Вы записаны!\n\n" + describe(appointment));
        } catch (SlotTakenException e) {
            send(chatId, e.getMessage(), Keyboards.times(slotService.getFreeSlots(state.getDate())));
        }
    }

    private void stopBooking(Long chatId, Long telegramId) {
        bookingStates.remove(telegramId);
        send(chatId, "Запись отменена.");
    }

    private void showMyAppointments(Long chatId, Long telegramId) {
        List<Appointment> appointments = appointmentService.getUpcoming(telegramId, LocalDate.now());
        if (appointments.isEmpty()) {
            send(chatId, "У вас нет предстоящих записей.");
            return;
        }
        for (Appointment appointment : appointments) {
            send(chatId, describe(appointment), Keyboards.cancelAppointment(appointment.getId()));
        }
    }

    private void cancelAppointment(Long chatId, Long telegramId, Long appointmentId) {
        Optional<Appointment> cancelled = appointmentService.cancel(appointmentId, telegramId);
        if (cancelled.isEmpty()) {
            send(chatId, "Запись не найдена. Возможно, она уже отменена.");
            return;
        }
        send(chatId, "Запись отменена:\n\n" + describe(cancelled.get()));
    }

    private String describe(Appointment appointment) {
        return Formats.DATE.format(appointment.getDate()) + ", " + Formats.TIME.format(appointment.getTime())
                + "\n" + appointment.getService().getName() + " — " + appointment.getService().getPrice() + " ₽";
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
        send(chatId, text, Keyboards.mainMenu());
    }

    private void send(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в чат {}", chatId, e);
        }
    }

    private void answerCallback(String callbackQueryId) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            log.error("Не удалось ответить на нажатие кнопки {}", callbackQueryId, e);
        }
    }
}
