package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class Bot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "Kino Bot Riga";
    }

    @Override
    public String getBotToken() {
        return "6507061463:AAGSxFwuG8wpjB7NgG_yUl4YoG2g2U_76CQ";
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);
    }
}
