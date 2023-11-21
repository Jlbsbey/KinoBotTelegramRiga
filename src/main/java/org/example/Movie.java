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

    ArrayList<Movie> Movies = new ArrayList<>();

    public void ScrapForumCinemas(){
        String url = "https://www.forumcinemas.lv/filmas/sobrid-kinoteatri";
        URL obj;
        try {
            obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

            //con.setRequestProperty("User-Agent", "Mozilla/5.0");

            //int responseCode = con.getResponseCode();
            //System.out.println("Response code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String html = response.toString();

            Document doc = Jsoup.parse(html);
            Elements movies = doc.getElementsByClass("event-list-item-inner");
            for (Element movie : movies) {
                Movies.add(elemensToStorage(movie));


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Movie elemensToStorage(Element movie){
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

    Movie(String lvName, String origName, String length, String startDate, String linkTo){
        this.lvName = lvName;
        this.origName = origName;
        this.length = length;
        this.linkTo = linkTo;
        this.startDate = startDate;
    }
}
