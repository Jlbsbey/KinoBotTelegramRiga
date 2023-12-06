package org.example;


import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertManyResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.mongodb.client.result.UpdateResult;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static String URI = "mongodb+srv://nikitasmorigo:sBxI7efFgKzbyrgK@cluster0.8bn2jvv.mongodb.net/?retryWrites=true&w=majority";
    static public Map<String, Movie> Movies = new HashMap<>();
    static public ArrayList<Movie> moviesOnUpdate = new ArrayList<>();
    public static void main(String[] args) throws TelegramApiException {
        //clearDB();
        Movie s;
        s= new Movie("-1", "-1", "-1", "-1", "-1", "-1");
        Movies = s.ScrapKino("apollo", "https://www.apollokino.lv/movies");
        addToMongo("apollo");
        Movies = s.ScrapKino("forum", "https://www.forumcinemas.lv/filmas/sobrid-kinoteatri");
        addToMongo("forum");
    }

    private static void addToMongo(String cinemaTheather){

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
            if(doc.getString(cinemaTheather).equals("true")){
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
            updateToDB(cinemaTheather, collection);
        }
        Movies.clear();
        moviesOnUpdate.clear();
    }

    private static void updateToDB(String cinemaTheater, MongoCollection<Document> collection){
        String apollo = "false";
        String forum = "false";
        String cinnamon = "false";
        switch (cinemaTheater){
            case "apollo":
                apollo = "true";
                break;
            case "forum":
                forum = "true";
                break;
            case "cinnamon":
                cinnamon = "true";
                break;
        }
        ArrayList<Document> inserts = new ArrayList<Document>();

        for (Movie mov : moviesOnUpdate){
            Document query = new Document().append("origID", mov.getOrigID() );
            // Creates instructions to update the values of three document fields
            Bson updates = Updates.combine(Updates.set(cinemaTheater, "true"));
            // Instructs the driver to insert a new document if none match the query
            UpdateOptions options = new UpdateOptions().upsert(false);
            // Updates the first document that has a "title" value of "Cool Runnings 2"
            UpdateResult result = collection.updateOne(query, updates, options);
        }

    }

    private static ArrayList<Document> MapToList(String cinemaTheater){
        String apollo = "false";
        String forum = "false";
        String cinnamon = "false";
        switch (cinemaTheater){
            case "apollo":
                apollo = "true";
                break;
            case "forum":
                forum = "true";
                break;
            case "cinnamon":
                cinnamon = "true";
                break;
        }
        Collection<Movie> values = Movies.values();
        ArrayList<Movie> listOfValues = new ArrayList<>(values);
        ArrayList<Document> inserts = new ArrayList<Document>();
        for (Movie mov : listOfValues){
            if(!mov.getLvName().isEmpty() && !mov.getOrigName().isEmpty()){
                inserts.add(new Document().append("lvName", mov.getLvName()).append("origName", mov.getOrigName()).append("length", mov.getLength()).append("startDate", mov.getStartDate()).append("linkTo", mov.getLinkTo()).append("apollo", apollo).append("forum", forum).append("cinnamon", cinnamon).append("origID", mov.getOrigID()));
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

/* переделать все в мапы
загружать из дб все в мап и проходить по мапу фильмов по ключу
1. собирать все в структуру муви и запихивать в мап, который потом возвращать в мейн (есть)
2. запрашивать все имеющиеся фильмы и запихивать их в мап
3. искать фильмы из дб в соскрапленном мапе и удалять те, кто совпадают
4. добавлять те, которые не нашлись
 */