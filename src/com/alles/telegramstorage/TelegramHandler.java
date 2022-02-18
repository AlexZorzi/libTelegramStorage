package com.alles.telegramstorage;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.response.GetFileResponse;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class TelegramHandler {
    private final String ChatID;
    private final TelegramBot TgBot;

    public TelegramHandler(String token, String chatID){
        this.TgBot = new TelegramBot(token);
        this.ChatID = chatID;
    }

    public void stop(){
        this.TgBot.shutdown();
    }

    public String getFilePath(String fileid) {
        GetFile request = new GetFile(fileid);
        GetFileResponse getFileResponse = this.TgBot.execute(request);
        com.pengrad.telegrambot.model.File file = getFileResponse.file();

        return this.TgBot.getFullFilePath(file);
    }
    public boolean Download(String fileid , File outFile) throws  IOException, InterruptedException {
            List<String> fileidList = List.of(fileid.split("\\."));
            String TempDir = "./TMP_"+fileidList.get(fileidList.size()-1)+"/";

            new File(TempDir).mkdir();

            ArrayList<File> Downloaded_Files = new ArrayList();
            CountDownLatch latch = new CountDownLatch(fileidList.size());
            for(int i = 0; i < fileidList.size(); i++){
                File destination = new File(TempDir+fileidList.get(fileidList.size()-1)+"."+(i+1));
                Downloaded_Files.add(destination);
                while (i >= fileidList.size() - ((int) latch.getCount()) + 4){
                    Thread.sleep(200);
                }
                ThreadDownload downloader = new ThreadDownload(this, fileidList.get(i), destination, latch);
                downloader.start();
            }
            latch.await();

            IOCopier.joinFiles(outFile, Downloaded_Files);
            FileUtils.deleteDirectory( new File(TempDir));
            return true;

    }



    public boolean DownloadZipped(String fileid , String outPath) throws  IOException, InterruptedException {
        if(fileid.split("\\.").length > 1){
            List<String> fileidList = List.of(fileid.split("\\."));
            String TempDir = "./TMP_"+fileidList.get(fileidList.size()-1)+"/";
            new File(TempDir).mkdir();
            CountDownLatch latch = new CountDownLatch(fileidList.size());
            for(int i = 0; i < fileidList.size(); i++){
                File currentZip;
                if (i == fileidList.size() - 1) {
                    currentZip = new File(TempDir+fileidList.get(fileidList.size()-1)+".zip");
                }else if( i < 9){
                    currentZip = new File(TempDir+fileidList.get(fileidList.size()-1)+".z0"+(i+1));
                }else {
                    currentZip = new File(TempDir+fileidList.get(fileidList.size()-1)+".z"+(i+1));

                }
                while (i >= fileidList.size() - ((int) latch.getCount()) + 4){
                    // Wait for threads
                }
                ThreadDownload downloader = new ThreadDownload(this, fileidList.get(i), currentZip, latch);
                downloader.start();
                //HandleFileDownload(fileidList.get(i), currentZip);
            }
            latch.await();

            new ZipFile(TempDir+fileidList.get(fileidList.size()-1)+".zip").mergeSplitFiles(new File(TempDir+"merged_"+fileidList.get(fileidList.size()-1)+".zip"));
            ZipFile Mergedzip = new ZipFile(TempDir+"merged_"+fileidList.get(fileidList.size()-1)+".zip");
            if (Mergedzip.getFileHeaders().size() > 1){
                Mergedzip.extractAll(outPath+fileidList.get(fileidList.size()-1)+"/");
            }else {
                Mergedzip.extractAll(outPath);
            }
            FileUtils.deleteDirectory( new File(TempDir));
            return true;
        }else {
            File file = new File(fileid+".zip");
            if (!file.exists()){
                HandleFileDownload(fileid, file);
                new ZipFile(file).extractAll(outPath);
                file.delete();
                return true;
            }else {
                return false;
            }
        }
    }

    public InputStream DownloadInputStream(String FileID) throws IOException, InterruptedException {
        String TmpFilename = FileID.split("\\.")[0];
        this.Download(FileID, new File("./"+TmpFilename));
        File result = new File("./"+TmpFilename);
        InputStream targetStream = new FileInputStream(result);
        new File("./"+TmpFilename).delete();
        return targetStream;
    }

    public void HandleFileDownload(String fileid, java.io.File output) throws IOException {
        InputStream inputStream = new URL(getFilePath(fileid)).openStream();
        Files.copy(inputStream, Paths.get(output.getPath()), StandardCopyOption.REPLACE_EXISTING);
    }

    public String Upload(File file) throws IOException, InterruptedException {
        if (file.exists()){
                ArrayList<File> Split_files = IOSplitter.DivideFile(file);
                CountDownLatch latch = new CountDownLatch(Split_files.size());
                int i = 0;
                CopyOnWriteArrayList<UploadedData> syncList = new CopyOnWriteArrayList<>();
                for (File current : Split_files) {
                    while (i >= syncList.size() + 4){
                        Thread.sleep(200);
                    }
                    ThreadUpload uploader = new ThreadUpload(this, current, latch, i, syncList);
                    uploader.start();
                    i = i + 1;
                }
                latch.await();
                FileUtils.deleteDirectory(new File("./TMP-"+file.getName()));
                StringBuilder splitID = OrderUploadedData(syncList);
                System.out.println(splitID);
                return splitID.toString();
            }else {
                return "";
            }
    }

    public String UploadZipped(File file) throws IOException, InterruptedException {
        if (file.exists()){
            String TempDir = "./TMP_"+file.getName()+"/";
            new File(TempDir).mkdir();
            if (Files.size(Path.of(file.getPath())) > 2e+7){
                ZipFile zipFile = new ZipFile(TempDir+file.getName()+".zip");
                zipFile.createSplitZipFile(List.of(file), new ZipParameters(), true, (long) 2e+7);

                CountDownLatch latch = new CountDownLatch(zipFile.getSplitZipFiles().size());
                int i = 0;
                CopyOnWriteArrayList<UploadedData> syncList = new CopyOnWriteArrayList<>();
                for (File currentZip : zipFile.getSplitZipFiles()) {
                    //String resultFileid = HandleFileUpload(currentZip);
                    while (i >= syncList.size() + 4){
                        // Wait for threads
                    }
                    ThreadUpload uploader = new ThreadUpload(this, currentZip, latch, i, syncList);
                    uploader.start();
                    i = i + 1;
                }
                latch.await();

                FileUtils.deleteDirectory(new File(TempDir));
                StringBuilder splitID = OrderUploadedData(syncList);
                return splitID.toString();
            }else {
                new ZipFile("./"+file.getName()+".zip").addFile(file);
                File zipUpload = new File(file.getName()+".zip");
                String splitID = HandleFileUpload(zipUpload);
                zipUpload.delete();
                return splitID;
            }
        }else {
            return null;
        }
    }

    private StringBuilder OrderUploadedData(CopyOnWriteArrayList<UploadedData> syncList) {
        StringBuilder splitID = new StringBuilder("");
        int n = 0;
        int last = 0;
        while (!syncList.isEmpty()){
            UploadedData curr = syncList.get(n);
            if (curr.Index == last){
                splitID.append(curr.Fileid+".");
                syncList.remove(n);
                last = last + 1;
            }
            if (n >= syncList.size() - 1 ){ n = 0 ;} else  {n = n + 1;}
        }
        return splitID;
    }


    public String HandleFileUpload(File file) throws IOException {
        Message message = this.TgBot.execute(new SendDocument(this.ChatID, file)).message();
        System.out.println(file.getName()+" uploaded as id: "+message.document().fileId());
        return message.document().fileId();
    }
}
