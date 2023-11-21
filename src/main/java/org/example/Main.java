package org.example;


import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws TelegramApiException {

        Movie s;
        s= new Movie("", "", "", "", "");
        s.ScrapForumCinemas();

    }
}