package com.alles.telegramstorage;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        String chatID = System.getenv("CHATID");
        String Token = System.getenv("TOKEN");
        if (chatID == null || Token == null){
            System.out.println("ENV vars CHATID and TOKEN not set");
            System.exit(1);
        }
        TelegramHandler tgBot = new TelegramHandler(Token, chatID);
        System.out.println("Input File: ");
        String dlID = tgBot.Upload(new File("/home/alles/Downloads/master.zip"));
        System.out.println("Download ID: "+dlID);
        boolean test = tgBot.Download(dlID, new File("/home/alles/test/master.zip"));
        tgBot.stop();
    }




}
