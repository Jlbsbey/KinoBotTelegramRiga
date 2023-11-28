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

public class Movie {
    private String lvName;
    private String origName;
    private String length;
    private String startDate;
    private String linkTo;
    //sBxI7efFgKzbyrgK
    ArrayList<Movie> Movies = new ArrayList<>();

    public void ScrapKino(String attr, String link){
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
    }

    public void ScrapForum(Document doc){
        Elements movies = doc.getElementsByClass("event-list-item-inner");
        for (Element movie : movies) {
            Movies.add(elemensToStorageForum(movie));
        }
    }

    public void ScrapApollo(Document doc){
        Elements movies = doc.getElementsByClass("schedule-item");
        for (Element movie : movies) {
            ScrapKino("apolloPage", movie.select(".movie-card__title").select("a").attr("href"));
        }
    }

    public void ScrapApolloPage(Document doc, String link){
        Elements movies = doc.getElementsByClass("main");
        for (Element movie : movies) {
            Movies.add(elemensToStorageApollo(movie, link));
        }
    }

    public Movie elemensToStorageForum(Element movie){
        String origName;
        if(movie.select(".event-original-name").text().isEmpty()){
            origName = movie.select(".event-name").not(".hidden").text();
        }else{
            origName = movie.select(".event-original-name").text();
        }

        return new Movie(movie.select(".event-name").not(".hidden").text(),
                origName, movie.select(".event-running-time").text(),
                movie.select(".event-releaseDate").text().split(": ")[1],
                "https://www.forumcinemas.lv" + movie.select(".event-name").not("hidden").select("a").attr("href"));
    }

    public Movie elemensToStorageApollo(Element movie, String link){
        String kinoTime = "";
        String kinoRelease;
        Elements times = movie.getElementsByClass("movie-details__item");
        for(Element time : times){
            if(time.select(".movie-details__key").text().equals("Filmas garums")){
                kinoTime = time.select(".movie-details__value").text();
            }
        }
        Elements releases = movie.getElementsByClass("movie-details__item");
        return new Movie(movie.select(".movie-details__title").text(),
                movie.select(".movie-details__original-title").text(),
                kinoTime,
                movie.select(".event-releaseDate").text(),
                link);
    }

    Movie(String lvName, String origName, String length, String startDate, String linkTo){
        this.lvName = lvName;
        this.origName = origName;
        this.length = length;
        this.linkTo = linkTo;
        this.startDate = startDate;
    }
}
