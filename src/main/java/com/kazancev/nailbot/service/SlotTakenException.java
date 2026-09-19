package com.kazancev.nailbot.service;

public class SlotTakenException extends RuntimeException {

    public SlotTakenException() {
        super("Это время уже занято. Пожалуйста, выберите другое.");
    }
}
