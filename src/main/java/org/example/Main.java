package org.example;


import com.mongodb.BasicDBList;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertManyResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.mongodb.client.result.UpdateResult;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.ws.rs.core.Link;
import java.awt.image.CropImageFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class Main {


    public static String URI = "";
    static public Map<String, Movie> Movies = new HashMap<>();
    static List<String> Cinemas = Arrays.asList(new String[6]);
    static public ArrayList<Movie> moviesOnUpdate = new ArrayList<>();
    public static void main(String[] args) throws TelegramApiException {
        //TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        //botsApi.registerBot(new Bot());
        setVar();
        //clearDB();
        Movie s;
        s= new Movie("-1", "-1", "-1", "-1", "-1", "-1");
        for(int i = 2; i<Cinemas.size()-2; i+=2){
            Movies = s.ScrapKino(Cinemas.get(i), Cinemas.get(i+1));
            //addToMongo(Cinemas.get(i));
        }
    }

    public static void setVar(){
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String config = "config.properties";
        try {
            Properties appProps = new Properties();
            appProps.load(new FileInputStream(rootPath + config));

            Cinemas.set(0, appProps.getProperty("apollo"));
            Cinemas.set(1, appProps.getProperty("apolloLink"));
            Cinemas.set(2, appProps.getProperty("forum"));
            Cinemas.set(3, appProps.getProperty("forumLink"));
            Cinemas.set(4, appProps.getProperty("cinnamon"));
            Cinemas.set(5, appProps.getProperty("cinnamonLink"));
            URI = appProps.getProperty("URI");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void addToMongo(String cinemaTheather){

        MongoClient mongoClient = MongoClients.create(URI);
        MongoDatabase database = mongoClient.getDatabase("filmCollection");
        MongoCollection<Document> collection = database.getCollection("filmCollectionKinoRiga");

        try{
            database.runCommand(new Document("ping", 1));
        }catch(MongoException e){
            e.printStackTrace();
        }

        FindIterable<Document> findIterable = collection.find(new Document());
        for(Document doc : findIterable){
            List<Boolean> tempCinemas = doc.getList("cinemas", Boolean.class);
            int i = 0;
            switch (cinemaTheather){
                case "apollo":
                    i=0;
                    break;
                case "forum":
                    i=1;
                    break;
                case "cinnamon":
                    i=2;
                    break;
            }
            if(tempCinemas.get(i)){
                Movies.remove(doc.getString("origID"));
            }else{
                if(Movies.get(doc.getString("origID")) != null){
                    moviesOnUpdate.add(Movies.get(doc.getString("origID")));
                    Movies.remove(doc.getString("origID"));
                }

            }
        }
        ArrayList<Document> movieList = MapToList(cinemaTheather);
        if(!movieList.isEmpty()){
            InsertManyResult result = collection.insertMany(movieList);
        }
        if(!moviesOnUpdate.isEmpty()){
            updateToDB(cinemaTheather, collection, findIterable);
        }
        Movies.clear();
        moviesOnUpdate.clear();
    }

    private static void updateToDB(String cinemaTheater, MongoCollection<Document> collection, FindIterable<Document> findIterable){
        BasicDBList inCinemas = new BasicDBList();
        BasicDBList Links = new BasicDBList();
        for (Movie mov : moviesOnUpdate){
            Links.clear();
            inCinemas.clear();
            for(Document it: findIterable){
                if(it.getString("origID").equals(mov.getOrigID())){
                    List<Boolean> tempCinemas = it.getList("cinemas", Boolean.class);
                    List<String> linkTo = it.getList("linkTo", String.class);
                    switch (cinemaTheater){
                        case "apollo":
                            inCinemas.add(true);
                            inCinemas.add(tempCinemas.get(1));
                            inCinemas.add(tempCinemas.get(2));
                            Links.add(mov.getLinkTo());
                            Links.add(linkTo.get(1));
                            Links.add(linkTo.get(2));
                            break;
                        case "forum":
                            inCinemas.add(tempCinemas.get(0));
                            inCinemas.add(true);
                            inCinemas.add(tempCinemas.get(2));
                            Links.add(linkTo.get(0));
                            Links.add(mov.getLinkTo());
                            Links.add(linkTo.get(2));
                            break;
                        case "cinnamon":
                            inCinemas.add(tempCinemas.get(0));
                            inCinemas.add(tempCinemas.get(1));
                            inCinemas.add(true);
                            Links.add(linkTo.get(0));
                            Links.add(linkTo.get(1));
                            Links.add(mov.getLinkTo());
                            break;
                    }
                }
            }
            Document query = new Document().append("origID", mov.getOrigID() );
            Bson updates = Updates.combine(Updates.set("cinemas", inCinemas), Updates.set("linkTo", Links));
            UpdateOptions options = new UpdateOptions().upsert(false);
            UpdateResult result = collection.updateOne(query, updates, options);
        }

    }

    private static ArrayList<Document> MapToList(String cinemaTheater){
        BasicDBList inCinemas = new BasicDBList();
        BasicDBList Links = new BasicDBList();
        switch (cinemaTheater){
            case "apollo":
                inCinemas.add(true);
                inCinemas.add(false);
                inCinemas.add(false);
                break;
            case "forum":
                inCinemas.add(false);
                inCinemas.add(true);
                inCinemas.add(false);
                break;
            case "cinnamon":
                inCinemas.add(false);
                inCinemas.add(false);
                inCinemas.add(true);
                break;
        }

        Collection<Movie> values = Movies.values();
        ArrayList<Movie> listOfValues = new ArrayList<>(values);
        ArrayList<Document> inserts = new ArrayList<Document>();
        for (Movie mov : listOfValues){
            Links.clear();
            switch (cinemaTheater){
                case "apollo":
                    Links.add(mov.getLinkTo());
                    Links.add("");
                    Links.add("");
                    break;
                case "forum":
                    Links.add("");
                    Links.add(mov.getLinkTo());
                    Links.add("");
                    break;
                case "cinnamon":
                    Links.add("");
                    Links.add("");
                    Links.add(mov.getLinkTo());
                    break;
            }
            if(!mov.getLvName().isEmpty() && !mov.getOrigName().isEmpty()){
                inserts.add(new Document().append("lvName", mov.getLvName()).append("origName", mov.getOrigName()).append("length", mov.getLength()).append("startDate", mov.getStartDate()).append("linkTo", Links).append("cinemas", inCinemas).append("origID", mov.getOrigID()));

            }
        }
        return inserts;
    }

    public static void clearDB() {
        MongoClient mongoClient = MongoClients.create(URI);
        MongoDatabase database = mongoClient.getDatabase("filmCollection");
        MongoCollection<Document> collection = database.getCollection("filmCollectionKinoRiga");
        collection.deleteMany(new Document());
    }
}
