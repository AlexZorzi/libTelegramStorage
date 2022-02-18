# Telgram(Infinite)Storage Lib
With this lib you can upload/download theoretically infinite content on Telegram Servers.

# Disclaimer
This works by uploading/downloading files like a normal bot would, so we have the same limitations that Bots have, For example the maximum size of files we can download is 20mb,
so we just need to splice them before uploading to get around this limitation

# What You Need
you just need to create a bot on [BotFather](https://t.me/BotFather), 
and get the Token like `1234567890:AAHqadAckv5dMLS47rKsTYD2u_zISsiIBrU`

Then you need to start the bot by sending him a message

And you need to know your Telegram ID so the bot knows in which chat Upload the files.
you can get your account ID by sending a message to [chatIDrobot](https://t.me/chatIDrobot). You will get something like this `1470338630`

# Usage

### - **CLI**

Upload:

  ``$ java -jar TelegramStorage.jar upload /Path/To/File``

``output: file.ext.zip uploaded as id: BQACAgEAAxkDAAP_YTeBrJ5Z9D1OXWw1ZyZBV_C-Zg0AAgsCAALMV8BFkYmTAniEJ3IgBA
``

_Note: ID will be longer for bigger files_

Download:

``$ java -jar TelegramStorage.jar Download *IDHERE* | OPTIONAL | /Path/To/Ouput/Dir/``

### - **Java Import**

```
import com.alles.telegramstoragelib.TelegramStorage;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        
        TelegramStorage Storage = new TelegramStorage("1226512047","1470338630:AAHqadAzxn5dMPV47rKsTYD2u_zORmzIBrU");
        String FileID = Storage.Upload(new File("/home/alles/Downloads/mumei.jpg"));
        boolean success = Storage.Download(FileID,"/home/alles/");
        
        if(success){
            System.out.Println("File Downloaded Successfully!");
        }else{
            System.out.Println("File Downloaded Failed!");
        }
    
    }
}
```

# Dependencies
```
- github.pengrad.java.telegram.bot.api
- net.lingala.zip4j
- commons.io
```
