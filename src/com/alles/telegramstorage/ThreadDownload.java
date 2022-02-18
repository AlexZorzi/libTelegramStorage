package com.alles.telegramstorage;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ThreadDownload extends java.lang.Thread {
    TelegramHandler BOT;
    String FILEID;
    File ZIPFILENAME;
    CountDownLatch LATCH;

    public ThreadDownload(TelegramHandler bot, String fileid, File zipFilename, CountDownLatch latch){
        this.BOT = bot;
        this.FILEID = fileid;
        this.ZIPFILENAME = zipFilename;
        this.LATCH = latch;
    }

    public void run(){
        try {
            this.BOT.HandleFileDownload(this.FILEID, this.ZIPFILENAME);
            this.LATCH.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
