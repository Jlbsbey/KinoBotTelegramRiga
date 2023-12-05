package org.example;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import org.bson.Document;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MongoClientConnectionExample {
    public static void main(String[] args) {
        String URI = "mongodb+srv://nikitasmorigo:<password>@cluster0.8bn2jvv.mongodb.net/?retryWrites=true&w=majority";
        String connectionUrl;

        //String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        //String config = "config.properties";

        connectionUrl = URI.replace("<password>", "sBxI7efFgKzbyrgK");
        MongoClient mongoClient = MongoClients.create(connectionUrl);

        MongoDatabase database = mongoClient.getDatabase("Mafia");
        MongoCollection<Document> collection = database.getCollection("Test");

        FindIterable<Document> documents = collection.find(new Document());

        for (Document document : documents) {
            System.out.println(document.getString("name"));
        }
    }
}
