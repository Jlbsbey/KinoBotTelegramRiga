package org.example;

import org.checkerframework.checker.units.qual.A;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class Session{
    final String date;
    final String time;
    final int freePlace;
    final int totalPlace;
    final String theather;
    final ArrayList<Double> prices;

    public Session(String date, String time, int freePlace, int totalPlace, ArrayList<Double> prices, String theather) {
        this.date = date;
        this.time = time;
        this.freePlace = freePlace;
        this.totalPlace = totalPlace;
        this.prices = prices;
        this.theather = theather;
    }
}

public class Movie {
    private String origID;
    private String lvName;
    private String origName;
    private String length;
    private String startDate;
    private String linkTo;
    private String tempName;
    private Movie tempMovie;


    public Map<String, Movie> Movies = new HashMap<>();
    private ArrayList<Session> times = new ArrayList<>();
    public Map<String, Movie> ScrapKino(String attr, String link){
        WebDriver webDriver = new ChromeDriver();
        String url = link;
        webDriver.get(url);
        Document doc = Jsoup.parse(webDriver.getPageSource());
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        webDriver.quit();
        switch (attr){
            case "forum":
                ScrapForum(doc);
            case "forumSession":
                ScrapForumSession(doc);
            case "apollo":
                ScrapApollo(doc);
            case "apolloPage":
                ScrapApolloPage(doc, link);
        }
        return Movies;
    }


    public void ScrapForum(Document doc){
        Elements movies = doc.getElementsByClass("right-side-top");
        for (Element movie : movies) {
            times.clear();
            tempName = "";
            tempMovie = elemensToStorageForum(movie);
            Movies.put(tempName, tempMovie);
        }
//        System.out.println(movies.size());
//        for(int i=0 ;i<2; i++){
//            times.clear();
//            tempName = "";
//            tempMovie = elemensToStorageForum(movies.get(i));
//            Movies.put(tempName, tempMovie);
//        }
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
        String linkOnSessions = SessionTimeForum("https://www.forumcinemas.lv" + movie.select(".event-name").not("hidden").select("a").attr("href"));
        tempName = origName.toLowerCase();
        ScrapKino("forumSession", linkOnSessions);
        final ArrayList<Session> tempTimes = new ArrayList<>(times);
        return new Movie(movie.select(".event-name").not(".hidden").text(),
                origName, movie.select(".event-running-time").text(),
                movie.select(".event-releaseDate").text().split(": ")[1],
                "https://www.forumcinemas.lv" + movie.select(".event-name").not("hidden").select("a").attr("href"), origName.toLowerCase(),
                tempTimes);
    }

    private String SessionTimeForum(String link) {
        Pattern pattern = Pattern.compile("/event/(\\d+)/");
        Matcher matcher = pattern.matcher(link);
        String ID = "";
        // Check if the pattern is found
        if (matcher.find()) {
            // Extract and parse the numeric value
            ID = matcher.group(1);
        }
        if(!ID.isEmpty()){
            System.out.println("https://www.forumcinemas.lv/websales/movie/" + ID + "/#page=%2Fwebsales%2Fmovie%2F"+ ID + "%2F%3Fdt%253D02.01.0001");
            return "https://www.forumcinemas.lv/websales/movie/" + ID + "/#page=%2Fwebsales%2Fmovie%2F"+ ID + "%2F%3Fdt%253D02.01.0001";
        }

        return null;
    }
//https://www.forumcinemas.lv/websales/movie/303712/#page=%2Fwebsales%2Fmovie%2F303712%2F%3Fdt%253D02.01.0001
    private void ScrapForumSession(Document doc) {
        Elements sessions = doc.getElementsByClass("show-list-item-inner");
        String time;
        ArrayList<Double> prices = new ArrayList<>();
        prices.add(12.20);
        prices.add(10.37);
        prices.add(7.70);
        prices.add(7.70);
        for (Element ses : sessions) {
            //System.out.println(ses);
            int freePlaces;
            if(ses.select(".freeSeats").text().isEmpty()){
                freePlaces = 0;
            }else{
                freePlaces = Integer.parseInt(ses.select(".freeSeats").text());
            }
            int totalPlaces;
            if(ses.select(".totalSeats").text().isEmpty()){
                totalPlaces = 0;
            }else{
                totalPlaces = Integer.parseInt(ses.select(".totalSeats").text());
            }

            time = ses.select(".showTime").text().split(" ")[1].trim();
            Session session = new Session(ses.select(".showDate").text().split(" ")[1].trim(), time,
                    freePlaces, totalPlaces,
                    prices, "forum");
//            System.out.println(session.date);
//            System.out.println(session.freePlace);
//            System.out.println(session.time);
//            System.out.println(session.totalPlace);
            times.add(session);
        }
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
                link, movie.select(".movie-details__original-title").text().toLowerCase(), times);
    }

    Movie(String lvName, String origName, String length, String startDate, String linkTo, String origID, ArrayList<Session> times){
        this.lvName = lvName;
        this.origName = origName;
        this.length = length;
        this.linkTo = linkTo;
        this.startDate = startDate;
        this.origID = origID;
        this.times = times;
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
    public ArrayList<Session> getTimes(){ return times;}
}
