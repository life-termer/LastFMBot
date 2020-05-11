import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Page {

    /*Get name*/
    public String getName(Document document) {
        Elements elements = document.getElementsByClass("header-new-title");
        String name = elements.text();
        name = "<strong>" + name + "</strong>";
        return name;
    }

    /*Get title of the page*/
    public String getTitle(Document document) {
        String title = document.title();
        title = title.replaceAll(" \\| Last.fm", "");
        title = "<strong>" + title + "</strong>";
        return title;
    }

    /*Create a HTML link*/
    public String getLink(String link, String inline) {
        return "<a href=\"" + link + "\">" + inline + "</a>";
    }

    /*Get short bio*/
    public String getDescription(Document document) {
        Elements elements = document.getElementsByClass("wiki-block-inner");
        String description = elements.text();
        return description.substring(0, 400) + "...";
    }
    /*Get some data about band of artist*/
    public String yearsActive(Document document) {
        Elements elements = document.getElementsByClass("catalogue-metadata-description");
        if (!elements.isEmpty()) {
            if (elements.size() == 1) return "Years Active: <strong>" + elements.get(0).text() + "</strong>\n";
            else return "Years Active (Born): <strong>" + elements.get(0).text() + "</strong>\nFrom: <strong>"
                    + elements.get(1).text() + "</strong>";
        }
        return "";
    }

    /*Get number of listeners and scrobbles*/
    public String getScrobbles(Document document) {
        Elements elements = document.getElementsByClass("intabbr js-abbreviated-counter");
        return "Listeners - <strong>" + elements.get(0).text() + "</strong> | Scrobbles - <strong>"
                + elements.get(1).text() + "</strong>";
    }

    /*Get info about artist, store it to string and return it*/
    public String artistsInfo(int x, String[] links) {
        String artistLink = "https://www.last.fm" + links[x];
        Document artist = null;
        try {
            artist = Jsoup.connect(artistLink).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert artist != null;
        return getName(artist)
                + "\n" + getLink(artistLink, "Last.FM")
                + "\n\n" + getScrobbles(artist)
                + "\n\n" + yearsActive(artist);
    }

    /*Get info about album, store it to string and return it*/
    public String albumsInfo(int x, String[] links){
        String albumLink = "https://www.last.fm" + links[x];
        Document album = null;
        try {
            album = Jsoup.connect(albumLink).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert album != null;
        Elements elements = album.getElementsByClass("catalogue-metadata-description");
        String date = " ( " + elements.get(1).text() + " )";
        return getTitle(album) + date
                + "\n" + getLink(albumLink, "Last.FM")
                + "\n\n" + getScrobbles(album);
    }


}
