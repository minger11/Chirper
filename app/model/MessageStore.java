package model;

import com.mongodb.MongoClient;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.DBCursor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters.*;
import com.mongodb.client.model.Sorts.*;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;
import org.bson.conversions.Bson.*;
import org.bson.conversions.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import org.bson.Document;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.*;
import java.util.regex.*;

import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonNode.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.json.JSONObject;
import org.json.JSONArray;

import controllers.UserController;
/**
 * Created by mingerso on 4/08/15.
 */

//the message store class
public class MessageStore {

    //creates a new instance of the messagestore
    public static final MessageStore instance = new MessageStore();

    //mongoclient
    protected MongoClient mongoClient;
    //define location of mongo server
    protected MessageStore() {
        mongoClient = new MongoClient("127.0.0.1", 27017);
    }

    //define mongo database
    protected MongoDatabase getDB() {
        return mongoClient.getDatabase("comp391_mingerso");
    }

    //define mongo collection within database
    protected MongoCollection<Document> getChitterCollection() {
        return getDB().getCollection("chitterMessage");
    }

    //define mongo collection within database
    protected MongoCollection<Document> getUserCollection() {
        return getDB().getCollection("chitterUser");
    }

    //create BSON from message
    protected static Document messageToBson(Message m) {
                return new Document("_id",new ObjectId(m.getId()))
                .append("userId", m.getUserId())
                .append("message", m.getMessage())
                        .append("time", m.getTime())
                        .append("tags", m.getTags());
    }

    protected static Message messageFromBson(Document d) {

        // This lets us call this method even if d is null
        if (d == null) {
            return null;
        }

        String id = d.getObjectId("_id").toHexString();
        String userId = d.getString("userId");
        String message = d.getString("message");
        Long time = d.getLong("time");
        Message m =  new Message(id, userId, message, time);

        return m;
    }

    //puts a new message in the message store
    public void storeMessage(Message message) {
        getChitterCollection().insertOne(messageToBson(message));
    }

    //returns the hashmap of the messagestore
    public HashMap<String, Message> getMessageStore(){
        //create a hashmap
        HashMap<String, Message> messages = new HashMap<>();
        FindIterable <Document> myDoc = getChitterCollection().find().sort(new Document("time",-1)).limit(30);
        MongoCursor<Document> cursor = myDoc.iterator();
        try {
            while (cursor.hasNext()) {
                Message m = messageFromBson(cursor.next());
                messages.put(m.getId(), m);
            }
        } finally {
            cursor.close();
        }
        return messages;
    }

    public ArrayNode getStoreJson(String tag) {
        //create a hashmap
        ArrayNode array = Json.newArray();
        FindIterable <Document> myDoc = getChitterCollection().find(new Document("tags", new Document("$regex", tag))).sort(new Document("time",-1)).limit(30);
        MongoCursor<Document> cursor = myDoc.iterator();
        try {
            while (cursor.hasNext()) {
                ObjectNode message = Json.newObject();
                Message m = messageFromBson(cursor.next());
                message.put("message", m.getMessage());
                message.put("user", m.getUserId());
                message.put("age", System.currentTimeMillis() - m.getTime());
                //get the user's email and put it in the objectnode
                message.put("email", getUserEmailById(new ObjectId(m.getUserId())));
                //String email = d.getString("email");
                array.add(message);
            }
        } finally {
            cursor.close();
        }
        return array;
    }

    public ArrayNode searchStore(String tag) {
        //create a hashmap
        ArrayNode array = Json.newArray();
        FindIterable <Document> myDoc;
        if(tag.substring(0,1).equals("@")){
            String userId = getUserIdByEmail(tag.substring(1));
            myDoc = getChitterCollection().find(new Document("userId", new Document("$regex", userId))).sort(new Document("time",-1)).limit(30);
        } else {
            myDoc = getChitterCollection().find(new Document("tags", new Document("$regex", tag))).sort(new Document("time", -1)).limit(30);
        }
        MongoCursor<Document> cursor = myDoc.iterator();
        try {
            while (cursor.hasNext()) {
                ObjectNode message = Json.newObject();
                Message m = messageFromBson(cursor.next());
                message.put("message", m.getMessage());
                message.put("user", m.getUserId());
                message.put("age", System.currentTimeMillis() - m.getTime());
                //get the user's email and put it in the objectnode
                message.put("email", getUserEmailById(new ObjectId(m.getUserId())));
                //String email = d.getString("email");
                array.add(message);
            }
        } finally {
            cursor.close();
        }
        return array;
    }

    public String getUserEmailById(ObjectId id) {
        Document d = getUserCollection().find(new Document("_id", id)).first();
        String email = d.getString("email");
        // I wrote userFromBson to accept nulls
        return email;
    }

    public String getUserIdByEmail(String email) {
        Document d = getUserCollection().find(new Document("email", email)).first();
        String userId = d.getObjectId("_id").toHexString();
        // I wrote userFromBson to accept nulls
        return userId;
    }

    /**
     * //returns the hashmap of the messagestore
    public ArrayNode getStoreJson(){
        //create a hashmap
            ArrayNode array = Json.newArray();

        FindIterable <Document> myDoc = getChitterCollection().find().sort(new Document("time",-1)).limit(30);
        MongoCursor<Document> cursor = myDoc.iterator();
        try {
            while (cursor.hasNext()) {

                ObjectNode message = Json.newObject();
                Message m = messageFromBson(cursor.next());
                message.put("message", m.getMessage());
                array.add(message);
            }
        } finally {
            cursor.close();
        }
        return array;
    }*/

    //returns a hashmap of all messages for the given user
    public HashMap<String, Message> getUserMessages(User user){
        //create a hashmap
        HashMap<String, Message> messages = new HashMap<>();
        FindIterable <Document> myDoc = getChitterCollection().find(new Document("userId", user.getId())).sort(new Document("time", -1)).limit(30);
        MongoCursor<Document> cursor = myDoc.iterator();
        try {
            while (cursor.hasNext()) {
                Message m = messageFromBson(cursor.next());
                messages.put(m.getId(), m);
            }
        } finally {
            cursor.close();
        }
        return messages;
    }

    //returns a hashmap of all messages with a hashtag containing the given string
    public HashMap<String, Message> getTaggedMessages(String tag) {
        //create a hashmap
        HashMap<String, Message> messages = new HashMap<>();
        FindIterable <Document> myDoc = getChitterCollection().find(new Document("tags", new Document("$regex",tag))).sort(new Document("time", -1)).limit(30);
        MongoCursor<Document> cursor = myDoc.iterator();
        try {
            while (cursor.hasNext()) {
                Message m = messageFromBson(cursor.next());
                messages.put(m.getId(), m);
            }
        } finally {
            cursor.close();
        }
        return messages;
    }

    //returns a hashmap of all messages with a hashtag that is exactly the string
    public HashMap<String, Message> getExactTaggedMessages(String tag){
        //create a hashmap
        HashMap<String, Message> messages = new HashMap<>();
        FindIterable <Document> myDoc = getChitterCollection().find(new Document("tags", tag)).sort(new Document("time", -1)).limit(30);
        MongoCursor<Document> cursor = myDoc.iterator();
        try {
            while (cursor.hasNext()) {
                Message m = messageFromBson(cursor.next());
                messages.put(m.getId(), m);
            }
        } finally {
            cursor.close();
        }
        return messages;
    }

}
