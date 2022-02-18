module TelegramStorage {
    requires zip4j;
    requires java.telegram.bot.api;
    requires commons.io;
    exports com.alles.telegramstoragelib;
    requires java.sql;
}