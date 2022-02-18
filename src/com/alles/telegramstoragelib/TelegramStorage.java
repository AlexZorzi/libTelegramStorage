package com.alles.telegramstoragelib;

import com.alles.telegramstorage.TelegramHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TelegramStorage {
    public String ChatID;
    public String Token;

    private TelegramHandler tgBot;

    public TelegramStorage(String chatid, String Token) {
        this.ChatID = chatid;
        this.Token = Token;
        this.tgBot = new TelegramHandler(Token, chatid);
    }

    public String Upload(File input) throws IOException, InterruptedException {
        String FiledID = this.tgBot.Upload(input);
        this.tgBot.stop();
        return FiledID;
    }

    public boolean Download(String FileID, String OutPutPath) throws IOException, InterruptedException {
        boolean result = this.tgBot.Download(FileID, new File(OutPutPath));
        this.tgBot.stop();
        return result;
    }

    public InputStream DownloadInputStream(String FileID) throws IOException, InterruptedException {
        String TmpFilename = FileID.split(".")[0];
        this.tgBot.Download(FileID, new File("./"+TmpFilename));
        File result = new File("./"+TmpFilename);
        InputStream targetStream = new FileInputStream(result);
        new File("./"+TmpFilename).delete();
        this.tgBot.stop();
        return targetStream;
    }

}
