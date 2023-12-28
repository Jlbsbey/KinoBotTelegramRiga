package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.example.Main.*;

public class Bot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "Kino Bot Riga";
    }
    public Bot(){
        setVar();
        clearDB();
        Movie s;
        Map<String, Double> prices = new HashMap<>();
        ArrayList<Session> ses = new ArrayList<>();
        s= new Movie("-1", "-1", "-1", "-1", "-1", "-1", ses);
        for(int i = 0; i<Cinemas.size()-2; i+=2){
            Movies = s.ScrapKino(Cinemas.get(i), Cinemas.get(i+1));
            addToMongo(Cinemas.get(i));
        }
    }

    @Override
    public String getBotToken() {
        String token = "";
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String config = "config.properties";
        try{
            Properties appProps = new Properties();
            appProps.load(new FileInputStream(rootPath + config));
            token = appProps.getProperty("token");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {

    }



    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
}
