import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        ApiContextInitializer.init();                       //Initialize API telegram
        TelegramBotsApi telegram = new TelegramBotsApi();   //Initialize class to access to API

        Bot bot = new Bot();
        bot.getPropValue();
        try{
            telegram.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
