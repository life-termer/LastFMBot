import org.jsoup.Jsoup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

public class Bot extends TelegramLongPollingBot {
    private long chat_id;       //Global variable to store chat ID
    public static String lastMessage;
    Band band;
    User user;
    String botToken;
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(); //Create keyboard

    /*Read config.properties values and get bot token from it*/
    public void getPropValue() throws FileNotFoundException {
        Properties properties = new Properties();
        String propFileName = "resources/config.properties";
        //load a properties file from class path, inside static method
        InputStream stream = getClass().getClassLoader().getResourceAsStream(propFileName);
        if(stream != null){
            try {
                properties.load(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
        //get the property value
        botToken = properties.getProperty("botToken");
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        update.getUpdateId();   //Update user information

        /* SendMessage - class for sending messages,
        setChatId - sets the ID to the person who wrote to the bot
        update.getMessage().getChatId() - get ID of the same person
         */
        SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
        chat_id = update.getMessage().getChatId();

        String messageText = update.getMessage().getText();     //Get the tex message
        sendMessage.setParseMode(ParseMode.HTML);

        sendMessage.setReplyMarkup(keyboardMarkup);     //Set the keyboard to the user
        try {
            sendMessage.setText(input(messageText));
            execute(sendMessage);       //Send message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {        //Get bot name
        return "@parse_LastFM_bot";
    }

    @Override
    public String getBotToken() {           //Get bot token
        return botToken;
    }

    public String input(String msg) {                           //Method to handle messages from user
        ArrayList<KeyboardRow> keyboard = new ArrayList<>();    //Create list of buttons
        KeyboardRow keyboardFirstRow = new KeyboardRow();       //First and second buttons rows
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        if (msg.equals("/start") || msg.equals("Menu")) {
            keyboard.clear();
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("The Latest Releases");
            keyboardFirstRow.add("Hot Right Now");
            keyboardFirstRow.add("Coming soon");
            keyboardSecondRow.add("Find Bands");
            keyboardSecondRow.add("Find Users");
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
            keyboardMarkup.setKeyboard(keyboard);
            lastMessage = msg;
            return "Hi! I'm LastFM bot and I can parse through Last.fm " +
                    "and show some information about bands and users. \uD83C\uDFB8\n" +
                    "Choose what are you looking for.";
        }
        if (msg.equals("The Latest Releases")) {
            lastMessage = msg;
            return getMusic(msg);
        }
        if (msg.equals("Hot Right Now")) {
            lastMessage = msg;
            return getMusic(msg);
        }
        if (msg.equals("Coming soon")) {
            lastMessage = msg;
            return getMusic(msg);
        }

        if (msg.equals("Find Bands")) {
            lastMessage = msg;
            return "Type the band or artist name";
        }
        if (msg.equals("Find Users")) {
            lastMessage = msg;
            return "Type the user name";
        }

        if (lastMessage.equals("Find Bands")) {
            if (checkLink(msg)) {
                msg = msg.replaceAll(" ", "+");
                keyboard.clear();
                keyboardFirstRow.clear();
                keyboardSecondRow.clear();
                keyboardFirstRow.add("Menu");
                keyboardFirstRow.add("Photos");
                keyboardFirstRow.add("Biography");
                keyboardFirstRow.add("Similar To");
                keyboardSecondRow.add("Top Tracks");
                keyboardSecondRow.add("Top Albums");
                keyboardSecondRow.add("Shoutbox");
                keyboardSecondRow.add("Latest Release");
                keyboard.add(keyboardFirstRow);
                keyboard.add(keyboardSecondRow);
                keyboardMarkup.setKeyboard(keyboard);
                lastMessage = msg;
                return getBandInfo(msg);
            }
            return "Seems there is no such band on Last FM.\nTry again \uD83D\uDC41️\u200D\uD83D\uDDE8";
        }
        if (msg.equals("Biography")) {
            return band.getWiki(band.link);
        }
        if (msg.equals("Latest Release")) {
            return getAlbums(0);
        }
        if (msg.equals("Top Albums")) {
            return getAlbums(1);
        }
        if (msg.equals("Top Tracks")) {
            return band.getTopTracks();
        }
        if (msg.equals("Shoutbox")) {
            return band.shoutbox();
        }
        if (msg.equals("Similar To")) {
            return getSimilarArtists();
        }
        if (msg.equals("Photos")) {
            lastMessage = msg;
            if (band.photosLinks.length == 40)
                return "I can show you first 40 photos from the gallery." +
                        "\nChoose the number \uD83D\uDC40";
            return "There are " + band.photosLinks.length + " photos in the gallery" +
                    "\nChoose the number \uD83D\uDC40";
        }
        if (lastMessage.equals("Photos")) {
            return getGalleryPhoto(msg);
        }

        if (lastMessage.equals("Find Users")) {
            if (checkLink(msg)) {
                keyboard.clear();
                keyboardFirstRow.clear();
                keyboardSecondRow.clear();
                keyboardFirstRow.add("Menu");
                keyboardFirstRow.add("Recent Tracks");
                keyboardSecondRow.add("Top Artists");
                keyboardSecondRow.add("Top Users Albums");
                keyboardSecondRow.add("Top Users Tracks");

                keyboard.add(keyboardFirstRow);
                keyboard.add(keyboardSecondRow);
                keyboardMarkup.setKeyboard(keyboard);
                lastMessage = msg;
                return getUserInfo(msg);
            }
            return "Seems there is no such user on Last FM.\nTry again \uD83D\uDC41️\u200D\uD83D\uDDE8";
        }

        if(msg.equals("Top Artists")){
            return user.getArtists();
        }
        if(msg.equals("Top Users Albums")){
            return user.getLibrary("/library/albums");
        }
        if(msg.equals("Top Users Tracks")){
            return user.getLibrary("/library/tracks");
        }
        if(msg.equals("Recent Tracks")){
            return user.getRecentTracks(50);
        }

        return "Choose what are you looking for.";
    }

    /*Check if the link is exist*/
    public boolean checkLink(String msg) {
        if (lastMessage.equals("Find Bands")) {
            try {
                Jsoup.connect("https://www.last.fm/music/" + msg).get();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        if (lastMessage.equals("Find Users")) {
            try {
                Jsoup.connect("https://www.last.fm/user/" + msg).get();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /*Get photo from link and send it with band info in caption*/
    public String getBandInfo(String msg) {

        band = new Band(msg);
        band.getPhotoLinks();
        band.getAlbumLinks();

        SendPhoto sendPhoto = new SendPhoto();
        try {
            InputStream inputStream = new URL(band.getBandImage(0)).openStream();  //Open stream with our link
            if (Files.exists(Paths.get("bandPhoto")))                   //Check if the file already exists
                Files.delete(Paths.get("bandPhoto"));
            Files.copy(inputStream, Paths.get("bandPhoto"));            //Copy image on disk and then send it
            sendPhoto.setChatId(chat_id);
            sendPhoto.setParseMode(ParseMode.HTML);                         //Parse mode to use HTML tags
            sendPhoto.setCaption(band.bandInfo());                          //Set caption with band info method
            sendPhoto.setPhoto(new File("bandPhoto"));
            execute(sendPhoto);
            Files.delete(Paths.get("bandPhoto"));                       //Delete photo from disk
            inputStream.close();                                            //Close stream
        } catch (TelegramApiException | IOException e) {
            e.printStackTrace();
            return band.bandInfo();
        }
        return "\uD83C\uDFB8";
    }

    /*Get photo from link, send it and return user info */
    public String getUserInfo(String msg){
        user = new User(msg);

        SendPhoto sendPhoto = new SendPhoto();
        try {
            InputStream inputStream = new URL(user.getAvatar()).openStream();
            if (Files.exists(Paths.get("bandPhoto")))
                Files.delete(Paths.get("bandPhoto"));
            Files.copy(inputStream, Paths.get("bandPhoto"));
            sendPhoto.setChatId(chat_id);
            sendPhoto.setParseMode(ParseMode.HTML);
            sendPhoto.setPhoto(new File("bandPhoto"));
            execute(sendPhoto);
            Files.delete(Paths.get("bandPhoto"));
            inputStream.close();
        } catch (TelegramApiException | IOException e) {
            e.printStackTrace();
        }
        return user.userInfo();
    }

    /*Get photo from link and send it*/
    public String getGalleryPhoto(String msg) {
        int index;
        SendPhoto sendPhoto = new SendPhoto();
        try {
            if (Files.exists(Paths.get("bandPhoto")))
                Files.delete(Paths.get("bandPhoto"));
            index = Integer.parseInt(msg);
            InputStream inputStream = new URL(band.getBandImage(index - 1)).openStream();
            Files.copy(inputStream, Paths.get("bandPhoto"));
            sendPhoto.setChatId(chat_id);
            sendPhoto.setPhoto(new File("bandPhoto"));
            execute(sendPhoto);
            Files.delete(Paths.get("bandPhoto"));
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong... Try again";
        }

        return "\uD83C\uDFB8";
    }
    /*Get photo from link and send it with album info in caption*/
    public String getAlbums(int x) {
        SendPhoto sendPhoto = new SendPhoto();
        if (x == 0) {       //for latest release
            try {
                if (Files.exists(Paths.get("bandPhoto")))
                    Files.delete(Paths.get("bandPhoto"));
                InputStream inputStream = new URL(band.getAlbumImage(x)).openStream();
                Files.copy(inputStream, Paths.get("bandPhoto"));
                sendPhoto.setChatId(chat_id);
                sendPhoto.setParseMode(ParseMode.HTML);
                sendPhoto.setCaption(band.getAlbumInfo());
                sendPhoto.setPhoto(new File("bandPhoto"));
                execute(sendPhoto);
                Files.delete(Paths.get("bandPhoto"));
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return band.getAlbumInfo();

            } catch (TelegramApiException e) {
                e.printStackTrace();
                return "Something went wrong... Try again";
            }
        } else {        //for top albums
            for (int i = 6; i < 10; i++) {
                try {
                    if (Files.exists(Paths.get("bandPhoto")))
                        Files.delete(Paths.get("bandPhoto"));
                    InputStream inputStream = new URL(band.getAlbumImage(i)).openStream();
                    Files.copy(inputStream, Paths.get("bandPhoto"));
                    sendPhoto.setChatId(chat_id);
                    sendPhoto.setParseMode(ParseMode.HTML);
                    sendPhoto.setCaption(band.getAlbumInfo());
                    sendPhoto.setPhoto(new File("bandPhoto"));
                    execute(sendPhoto);
                    Files.delete(Paths.get("bandPhoto"));
                    inputStream.close();
                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                    return "Something went wrong... Try again";
                }
            }
        }
        return "\uD83C\uDFB8";
    }

    /*Send message with artist info*/
    public String getSimilarArtists() {
        SendMessage sendMessage = new SendMessage().setChatId(chat_id);
        sendMessage.setParseMode(ParseMode.HTML);
        for (int i = 3; i < 6; i++) {
            sendMessage.setText(band.artistsInfo(i, band.albumsLinks));
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return "\uD83C\uDFB8";
    }
    /*Send messages using parent class methods and Music class arrays with links*/
    public String getMusic(String msg) {
        SendMessage sendMessage = new SendMessage().setChatId(chat_id);
        sendMessage.setParseMode(ParseMode.HTML);

        if (msg.equals("Hot Right Now")) {
            Music.getInstance().getHRNLinks();
            for (int i = 0; i <  Music.getInstance().hotRightNowLinks.length; i++) {
                sendMessage.setText( Music.getInstance().artistsInfo(i,  Music.getInstance().hotRightNowLinks));
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        if (msg.equals("The Latest Releases")) {
            Music.getInstance().getLRLinks();
            for (int i = 0; i <  Music.getInstance().latestReleasesLinks.length; i++) {
                sendMessage.setText( Music.getInstance().albumsInfo(i,  Music.getInstance().latestReleasesLinks));
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        if (msg.equals("Coming soon")) {
            Music.getInstance().getCSLinks();
            for (int i = 0; i <  Music.getInstance().comingSoonLinks.length; i++) {
                sendMessage.setText( Music.getInstance().albumsInfo(i,  Music.getInstance().comingSoonLinks));
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        return "\uD83C\uDFB8";
    }
}
