import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;

public class Music extends Page {
    private static Music instance;          //Instance of Music class
    public Document music;                  //Document to parse music page
    public Document outNow;                 //Document to parse new release page
    public Document comingSoon;             //Document to parse coming soon page
    public String[] latestReleasesLinks;    //Array links to latest releases pages
    public String[] hotRightNowLinks;       //Array links to popular artists pages
    public String[] comingSoonLinks;        //Array links to coming soon releases pages

    private Music () {
        connect();
    }

    /*Lazy initialization method to implement Singleton pattern*/
    public static Music getInstance(){
        if(instance == null){
            instance = new Music();
        }
        return instance;
    }

    /*Connect to the web pages*/
    private void connect() {
        try {
            music = Jsoup.connect("https://www.last.fm/music/").get();
            outNow = Jsoup.connect("https://www.last.fm/music/+releases/out-now").get();
            comingSoon = Jsoup.connect("https://www.last.fm/music/+releases/coming-soon").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*Get links to new releases, store them into array of strings */
    public void getLRLinks(){
        Elements elements = outNow.getElementsByClass("\n" +
                "            js-link-block-cover-link\n" +
                "            link-block-cover-link\n" +
                "        ");
        latestReleasesLinks = new String[20];
        for (int i = 0; i < 20; i++) {
            String element = elements.get(i).attr("href");
            latestReleasesLinks[i] = element;
        }
    }

    /*Get links to popular artists, store them into array of strings */
    public void getHRNLinks(){
        Elements elements = music.getElementsByClass("music-featured-item-heading-link");
        hotRightNowLinks = new String[2];
            hotRightNowLinks[0] = elements.get(2).attr("href");
            hotRightNowLinks[1] = elements.get(3).attr("href");
    }

    /*Get links to coming soon releases, store them into array of strings */
    public void getCSLinks(){
        Elements elements = comingSoon.getElementsByClass("\n" +
                "            js-link-block-cover-link\n" +
                "            link-block-cover-link\n" +
                "        ");
        comingSoonLinks = new String[20];
        for (int i = 0; i < 20; i++) {
            String element = elements.get(i).attr("href");
            comingSoonLinks[i] = element;
        }
    }
}
