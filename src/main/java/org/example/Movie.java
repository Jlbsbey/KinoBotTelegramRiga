package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Movie {
    private String origID;
    private String lvName;
    private String origName;
    private String length;
    private String startDate;
    private String linkTo;
    private String tempName;
    private Movie tempMovie;

    //public ArrayList<Movie> Movies = new ArrayList<>();
    public Map<String, Movie> Movies = new HashMap<>();

    public Map<String, Movie> ScrapKino(String attr, String link){
        String url = link;
        URL obj;
        try {
            obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String html = response.toString();

            Document doc = Jsoup.parse(html);
            switch (attr){
                case "forum":
                    ScrapForum(doc);
                case "apollo":
                    ScrapApollo(doc);
                case "apolloPage":
                    ScrapApolloPage(doc, link);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Movies;
    }

    public void ScrapForum(Document doc){
        Elements movies = doc.getElementsByClass("right-side-top");
        for (Element movie : movies) {
            tempMovie = elemensToStorageForum(movie);
            Movies.put(tempName, tempMovie);
            tempName = "";
        }
    }

    public void ScrapApollo(Document doc){
        Elements movies = doc.getElementsByClass("schedule__item");
        for (Element movie : movies) {
            ScrapKino("apolloPage", movie.select(".movie-card__title").select("a").attr("href"));
        }
    }

    public void ScrapApolloPage(Document doc, String link){
        Elements movies = doc.getElementsByClass("main");
        for (Element movie : movies) {
            tempMovie = elemensToStorageApollo(movie, link, doc);
            Movies.put(tempName, tempMovie);
            tempName = "";
        }
    }

    public Movie elemensToStorageForum(Element movie){
        String origName;
        if(movie.select(".event-original-name").text().isEmpty()){
            origName = movie.select(".event-name").not(".hidden").text();
        }else{
            origName = movie.select(".event-original-name").text();
        }
        tempName = origName.toLowerCase();
        return new Movie(movie.select(".event-name").not(".hidden").text(),
                origName, movie.select(".event-running-time").text(),
                movie.select(".event-releaseDate").text().split(": ")[1],
                "https://www.forumcinemas.lv" + movie.select(".event-name").not("hidden").select("a").attr("href"), origName.toLowerCase());
    }

    public String getKinoTimeApollo(Element movie, Document doc){
        Elements times = doc.getElementsByClass("movie-details__item");
        for(Element time : times){
            if(time.select(".movie-details__key").text().equals("Filmas garums")){
                return time.select(".movie-details__value").text();
            }
        }
        return "";

    }

    public String getKinoReleaseApollo(Element  movie, Document doc){
        Elements releases = doc.getElementsByClass("grid__col--md-6");
        for(Element release : releases){
            if(release.select(".specs__key").text().equals("Kinoteātros no")){
                return release.select(".specs__value").text();
            }
        }
        return "";



    }

    public Movie elemensToStorageApollo(Element movie, String link, Document doc){
        String kinoTime = getKinoTimeApollo(movie, doc);
        String kinoRelease = getKinoReleaseApollo(movie, doc);
        tempName = movie.select(".movie-details__original-title").text();
        return new Movie(movie.select(".movie-details__title").text(),
                movie.select(".movie-details__original-title").text(),
                kinoTime,
                kinoRelease,
                link, movie.select(".movie-details__original-title").text().toLowerCase());
    }

    Movie(String lvName, String origName, String length, String startDate, String linkTo, String origID){
        this.lvName = lvName;
        this.origName = origName;
        this.length = length;
        this.linkTo = linkTo;
        this.startDate = startDate;
        this.origID = origID;
    }

    public String getLvName() {
        return lvName;
    }

    public String getOrigName() {
        return origName;
    }

    public String getLength() {
        return length;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getLinkTo() {
        return linkTo;
    }

    public String getOrigID() {
        return origID;
    }
}
