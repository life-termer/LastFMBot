import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Band extends Page {
    private Document document;      //Document to parse band page.
    private Document wiki;          //Document to parse wiki page
    private Document image;         //Document to parse image gallery
    private Document album;         //Document to parse albums pics
    private Document tracks;        //Document to parse top tracks page
    private Document shouts;        //Document to parse shoutbox

    public String name;             //Name of the band
    public String link;             //Link to the main band page
    private String albumLink;       //Link on album
    public String[] photosLinks;    //Array for photos links in the gallery;
    public String[] albumsLinks;    //Array for albums links in the gallery;

    public Band(String msg) {
        name = msg;
        link = "https://www.last.fm/music/" + name;
        connect();
    }

    private void connect() {         //Connect to the web page
        try {
            document = Jsoup.connect(link).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*Get band info with parent class methods*/
    public String bandInfo() {
        return getName(document)
                + "\n" + getLink(link, "Last.FM")
                + "\n\n" + getScrobbles(document)
                + "\n\n" + yearsActive(document)
                + "\n\n" + getDescription(document);
    }

    /*Get photo URL from gallery*/
    public String getBandImage(int x) {
        String link = "https://www.last.fm" + photosLinks[x];
        try {
            image = Jsoup.connect(link).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements elements = image.getElementsByClass("js-gallery-image");
        return elements.attr("src");
    }
    /*Get album cover URL from album page*/
    public String getAlbumImage(int x) {
        albumLink = "https://www.last.fm" + albumsLinks[x];
        try {
            album = Jsoup.connect(albumLink).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document tempA = null;
        Elements temp = album.getElementsByClass("cover-art");
        String linkB = "https://www.last.fm" + temp.attr("href");
        try {
            tempA = Jsoup.connect(linkB).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert tempA != null;
        Elements elements = tempA.getElementsByClass("js-gallery-image");
        return elements.attr("src");
    }
    /*Get ful biography from wiki page*/
    public String getWiki(String link) {
        String wikiText = "";
        try {
            wiki = Jsoup.connect(link + "/+wiki").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements elements = wiki.getElementsByClass("wiki-content");
        wikiText = elements.text();
        if (!wikiText.equals("")) {         //Check if there is a wiki
            if (wikiText.length() > 3500)   //Check if message is not too long
                wikiText = wikiText.substring(0, 3500)
                        + " ...\n\n"
                        + getLink(link + "/+wiki", "Continue on Last.fm");

        } else wikiText = "<i>Do you know any background info about this?</i>\n\n"
                + getLink(link + "/+wiki", "Start the wiki");
        return wikiText;
    }
    /*Get links of gallery pages and store them into array of strings*/
    public void getPhotoLinks() {
        try {
            image = Jsoup.connect(link + "/+images/").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements elements = image.getElementsByClass("image-list-item");
        photosLinks = new String[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            String element = elements.get(i).attr("href");
            photosLinks[i] = element;
        }
    }

    /*Get links of album pages and store them into array of strings*/
    public void getAlbumLinks() {
        Elements elements = document.getElementsByClass("\n" +
                "            js-link-block-cover-link\n" +
                "            link-block-cover-link\n" +
                "        ");
        albumsLinks = new String[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            String element = elements.get(i).attr("href");
            albumsLinks[i] = element;
        }
    }

    /*Get album info, store it into string and return it*/
    public String getAlbumInfo() {
        String albumInfo = "";
        Elements elements = album.getElementsByClass("header-new-title");
        albumInfo += "<strong>" + elements.text() + "</strong> ( ";
        elements = album.getElementsByClass("catalogue-metadata-description");
        albumInfo += elements.get(1).text() + " )\n\n" + getScrobbles(album);

        String wiki = getWiki(albumLink);
        if (wiki.contains("<i>Do you know any background info about this?</i>\n\n"))
            return albumInfo + "\n\n" + wiki;
        if (wiki.length() > 400)
            wiki = wiki.substring(0, 400) + " ...";

        albumInfo += "\n\n" + wiki + "\n\n" + getLink(albumLink + "/+wiki", " Continue on Last.fm");
        return albumInfo;
    }

    /*Get tracks info, store it into string and return it*/
    public String getTopTracks() {
        String topTracks = "";
        try {
            tracks = Jsoup.connect(link + "/+tracks/").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements elements = tracks.getElementsByAttribute("title"); //get tracks titles
        List<String> titles = new ArrayList<>();
        for (Element element : elements) {
            if (!element.text().equals("Play track"))
                titles.add(element.text());
        }
        elements = tracks.getElementsByClass("chartlist-count-bar-value");  //get number of listeners
        for (int i = 0; i < 15; i++) {                                               //get them together into string
            topTracks += i + 1 + ". <strong>" + titles.get(i) + "</strong>  -  "
                    + elements.get(i).text() + "\n\n";
        }
        return topTracks;
    }

    /*Get info from the shoutbox page, store it into string and return it*/
    public String shoutbox() {
        try {
            shouts = Jsoup.connect(link + "/+shoutbox").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String shoutbox = "";
        Elements nickNames = shouts.getElementsByClass("link-block-target");
        Elements messages = shouts.getElementsByClass("shout-body");
        Elements dates = shouts.getElementsByClass("shout-permalink shout-timestamp");
        for (int i = 0; i < messages.size(); i++) {
            if (i == 30 || (shoutbox.length() + messages.get(i).text().length() > 3900)) break;
            else {
                String message = messages.get(i).text();
                message = message.replaceAll("<", "");
                message = message.replaceAll(">", "");
                shoutbox += "<strong>" + nickNames.get(i).text() + "</strong> <i>("
                        + dates.get(i).text() + ")</i>\n"
                        + message + "\n\n";
            }
        }
        return shoutbox;
    }
}
