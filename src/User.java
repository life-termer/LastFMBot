import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class User extends Page {
    public Document document;   //Document to parse user page
    public String name;         //User nick name
    public String link;         //Link to users page

    public User(String msg) {
        name = msg;
        link = "https://www.last.fm/user/" + name;
        connect();
    }

    private void connect() {         //Connect to the web page
        try {
            document = Jsoup.connect(link).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*Get user info */
    public String userInfo() {
        return getName() + getDate()
                + "\n" + getLink(link, "Last.FM")
                + "\n\n" + getStats() + "\n\n<Strong>Recent Tracks:</strong> "
                + "\n\n" + getRecentTracks(10);
    }

    public String getName() {
        Elements elements = document.getElementsByClass("header-title-display-name");
        return "<strong>" + elements.text() + "</strong>";
    }

    public String getDate() {
        Elements elements = document.getElementsByClass("header-scrobble-since");
        return " " + elements.text();
    }

    public String getStats() {
        Elements elements = document.getElementsByClass("header-metadata-display");
        return "Scrobbles - <strong>" + elements.get(0).text() + "</strong>"
                + "\n<i>" + elements.attr("title") + "</i>"
                + "\n" + "Artists - <strong>" + elements.get(1).text() + "</strong>"
                + "\n" + "Loved Tracks - <strong>" + elements.get(2).text() + "</strong>";
    }

    /*Get user avatar picture link*/
    public String getAvatar(){
        String link;
        Elements elements = document.getElementsByAttribute("content");
        link = elements.get(8).attr("content");
        return link;
    }

    /*Get user recent tracks, store them into string and return it */
    public String getRecentTracks(int x) {
        Document scrobbles = null;
        String recentTracks = "";
        try {
            scrobbles = Jsoup.connect(link + "/library").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert scrobbles != null;
        Elements elements = scrobbles.getElementsByClass("chartlist-artist"); //get artist name
        List<String> artists = new ArrayList<>();
        for (Element element : elements) {
            artists.add(element.text());
        }
        elements = scrobbles.getElementsByClass("chartlist-name");  //get track name
        List<String> names = new ArrayList<>();
        for (Element element : elements) {
            names.add(element.text());
        }
        elements = scrobbles.getElementsByClass("\n" +          //get last played time
                "                chartlist-timestamp\n" +
                "                chartlist-timestamp--lang-en\n" +
                "            ");
        List<String> times = new ArrayList<>();
        for (Element element : elements) {
            times.add(element.text());
        }

        if (x > names.size()) x = names.size();
        for (int i = 0; i < x; i++)         //store them together
            recentTracks += i + 1 + ". <strong>" + names.get(i) + "</strong> - "
                    + artists.get(i) + "  <i>" + times.get(i) + "</i>\n\n";
        return recentTracks;
    }

    /*Get user top artists, store them into string and return it */
    public String getArtists(){
        Document artists = null;
        String art = "";
        try {
            artists = Jsoup.connect(link + "/library/artists").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert artists != null;
        Elements elements = artists.getElementsByClass("chartlist-name");//get artists names
        List<String> names = new ArrayList<>();
        for (Element element : elements) {
            names.add(element.text());
        }
        elements = artists.getElementsByClass("chartlist-count-bar-value");//get number of scrobblers
        List<String> scr = new ArrayList<>();
        for (Element element : elements) {
            scr.add(element.text());
        }
        for(int i = 0; i < names.size(); i++)
            art += i + 1 + ". <strong>" + names.get(i) + "</strong> - "
                    + scr.get(i) + "\n\n";
        return art;
    }

    /*Get user top track, store them into string and return it */
    public String getLibrary(String url){
        Document library = null;
        String lbr = "";
        try {
            library = Jsoup.connect(link + url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert library != null;
        Elements elements = library.getElementsByClass("chartlist-name");   //get tracks names
        List<String> names = new ArrayList<>();
        for (Element element : elements) {
            names.add(element.text());
        }
        elements = library.getElementsByClass("chartlist-artist");      //get artist names
        List<String> artists = new ArrayList<>();
        for (Element element : elements) {
            artists.add(element.text());
        }
        elements = library.getElementsByClass("chartlist-count-bar-value"); //get number of scrobblers
        List<String> scr = new ArrayList<>();
        for (Element element : elements) {
            scr.add(element.text());
        }
        for(int i = 0; i < names.size(); i++){      //store them together
            lbr += i + 1 + ". <strong>" + names.get(i) + "</strong> - "
                    + artists.get(i) + "  <i>" + scr.get(i) + "</i>\n\n";
        }
        return lbr;
    }
}
