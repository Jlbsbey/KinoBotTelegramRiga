package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;

public class Bot extends TelegramLongPollingBot {
    ArrayList<Long> IDs = new ArrayList<>();
    boolean isInput = false;
    String lastCommand = "";
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
        var msg = update.getMessage();
        var user = msg.getFrom();
        ifExists(user.getId());
        if(msg.isCommand()){
            if(msg.getText().equals("/echoall")){
                echoAll(user.getId(), false, ".");
            }
        }
        if(isInput == true){
            switch (lastCommand){
                case "echoAll":
                    echoAll(user.getId(), true, msg.getText());
                    break;
            }
        }

    }


    public void echoAll(Long userID, boolean time, String text){
        if(!time) {
            sendText(userID, "Send your text to echo");
            isInput = true;
            lastCommand = "echoAll";
        }else{
            for(Long id: IDs){
                sendText(id, text);
                isInput = false;
            }
        }
    }

    public boolean ifExists(Long userID){
        boolean appears= false;
        for (Long id : IDs) {
            if (userID.equals(id)) {
                appears = true;
                return true;
            }
        }
        if(appears==false){
            addUser(userID);
        }
        return false;
    }

    public void addUser(Long userID){
        IDs.add(userID);
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
