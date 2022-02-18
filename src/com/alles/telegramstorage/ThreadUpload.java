package com.alles.telegramstorage;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class ThreadUpload extends java.lang.Thread {
    TelegramHandler BOT;
    File ZIPFILE;
    CountDownLatch LATCH;
    int INDEX;
    CopyOnWriteArrayList<UploadedData> SYNCLIST;

    public ThreadUpload(TelegramHandler bot, File zipFile, CountDownLatch latch, int index, CopyOnWriteArrayList<UploadedData> syncList){
        this.BOT = bot;
        this.ZIPFILE = zipFile;
        this.LATCH = latch;
        this.INDEX = index;
        this.SYNCLIST = syncList;
    }

    public void run(){
        try {
            String result = this.BOT.HandleFileUpload(this.ZIPFILE);
            UploadedData data = new UploadedData();
            data.Fileid = result;
            data.Index = this.INDEX;
            this.SYNCLIST.add(data);
            this.ZIPFILE.delete();
            this.LATCH.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class UploadedData{
    public String Fileid;
    public int Index;
}