package com.alles.telegramstorage;

import java.io.*;
import java.util.ArrayList;

public class IOSplitter {
    public static ArrayList<File> DivideFile(File file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        long numSplits = (long) (Math.ceil(raf.length() / 2e+7)); //from user input, extract it from args
        long sourceSize = raf.length();
        long bytesPerSplit = sourceSize/numSplits ;
        long remainingBytes = sourceSize % numSplits;
        String TMP_Dir = "./TMP-"+file.getName()+"/";
        new File(TMP_Dir).mkdir();
        ArrayList<File> File_Split_List = new ArrayList<>();
        int maxReadBufferSize = (int) 2e+7;
        for(int destIx=1; destIx <= numSplits; destIx++) {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(TMP_Dir+file.getName()+"-split."+destIx));
            File_Split_List.add(new File(TMP_Dir+file.getName()+"-split."+destIx));
            if(bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit/maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for(int i=0; i<numReads; i++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if(numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            }else {
                readWrite(raf, bw, bytesPerSplit);
            }
            bw.close();
        }
        if(remainingBytes > 0) {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(TMP_Dir+file.getName()+"-split."+(numSplits+1)));
            File_Split_List.add(new File(TMP_Dir+file.getName()+"-split."+(numSplits+1)));
            readWrite(raf, bw, remainingBytes);
            bw.close();
        }
        raf.close();
        return File_Split_List;
    }

    static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if(val != -1) {
            bw.write(buf);
        }
    }
}
