package model;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
/**
 * Created by mingerso on 30/07/15.
 */

//the userservice class
public class UserService {

    //creates a new instance of the userservice
    public static final UserService instance = new UserService();

    //mongoclient
    protected MongoClient mongoClient;
    //define location of mongo server
    protected UserService() {
        mongoClient = new MongoClient("127.0.0.1", 27017);
    }

    //define mongo database
    protected MongoDatabase getDB() {
        return mongoClient.getDatabase("comp391_mingerso");
    }

    //define mongo collection within database
    protected MongoCollection<Document> getChitterCollection() {
        return getDB().getCollection("chitterUser");
    }

    //Allocates an ObjectID and returns it as a hex string; I've exposed this so we can use it also for session IDs.
    public String allocateId() {
        return new ObjectId().toHexString();
    }

    //Checks if this is a valid ObjectID, as some browsers might have old UUIDs cached
    public boolean isValidId(String id) {
        try {
            ObjectId i = new ObjectId(id);
            return i.toHexString().equals(id);
        } catch (Exception ex) {
            return false;
        }
    }

    //create BSON from user
    protected static Document userToBson(User u) {
        List<Document> sessions = new ArrayList<>();
        for (Session s : u.getSessions()) {
            sessions.add(sessionToBson(s));
        }

        return new Document("_id", new ObjectId(u.getId()))
                .append("email", u.getEmail())
                   .append("hash", u.getHash())
                .append("sessions", sessions);
    }

    protected static User userFromBson(Document d) {

        // This lets us call this method even if d is null
        if (d == null) {
            return null;
        }

        String id = d.getObjectId("_id").toHexString();
        String email = d.getString("email");
        String hash = d.getString("hash");
        User u =  new User(id, email, hash);

        // This gives an unchecked warning; we'd need to use the safer means of doing this (which we don't cover)
        // to avoid the warning
        List<Document> sessions = d.get("sessions", List.class);

        if(sessions != null) {
            for (Document sd : sessions) {
                Session s = sessionFromBson(sd);
                u.newUserSession(s.getId(), s);
            }
        }

        return u;
    }

    protected static Session sessionFromBson(Document d) {
        // This lets us call this method even if d is null
        if (d == null) {
            return null;
        }

        String id = d.getObjectId("_id").toHexString();
        String ip = d.getString("ipAddress");
        long since = d.getLong("since");
        return new Session(id, ip, since);
    }

    protected static Document sessionToBson(Session s) {
        return new Document("_id", new ObjectId(s.getId()))
                .append("ipAddress", s.getIpAddress())
                .append("since", s.getSince());
    }

    //stores a user in the user hashmap
    public User registerUser(User u) {
        //insert user to mongo
        getChitterCollection().insertOne(userToBson(u));
        return u;
    }

    public void update(User u) {
        getChitterCollection().replaceOne(new Document("_id", new ObjectId(u.getId())), userToBson(u));
    }

    public User getUserById(String id) {
        Document d = getChitterCollection().find(new Document("_id", id)).first();

        // I wrote userFromBson to accept nulls
        return userFromBson(d);
    }

 //Get the user by email and password, returning null if they don't exist (or the password is wrong
    public User getUser(String email, String password) {
        Document d = getChitterCollection().find(new Document("email", email)).first();

        // I wrote userFromBson to accept nulls
        User u = userFromBson(d);
        if (u != null && BCrypt.checkpw(password, u.getHash())) {
            return u;
        } else {
            return null;
        }
    }

    //Get the user by email, returning null if they don't exist
    public User getUser(String email) {
        Document d = getChitterCollection().find(new Document("email", email)).first();

        // I wrote userFromBson to accept nulls
        return userFromBson(d);
    }

    //returns a user based on the sessionId
    public User getUserFromSession(String sessionId) {
        Document d = getChitterCollection().find(new Document("sessions._id", new ObjectId(sessionId))).first();
        return userFromBson(d);
    }

    //checks if a user exists in the database
    public Boolean userExists(String email) {
        Document d = getChitterCollection().find(new Document("email", email)).first();

        // I wrote userFromBson to accept nulls
        User u = userFromBson(d);
        if (u != null) {return true;}
        else {return false;}
    }
}