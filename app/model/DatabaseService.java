package model;

import com.fasterxml.jackson.databind.JsonNode.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters.*;
import com.mongodb.client.model.Sorts.*;
import org.bson.Document;
import org.bson.conversions.*;
import org.bson.conversions.Bson.*;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;
import play.libs.Json;

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import static com.mongodb.client.model.Filters.*;

/**
 * Created by mingerso on 4/08/15.
 */

//the message database class
public class DatabaseService {

    //---------INITIALIZE DATABASE AND COLLECTIONS -------------------------------

    //creates a new database of the messagestore
    public static final DatabaseService instance = new DatabaseService();

    //mongoclient
    protected MongoClient mongoClient;

    //define location of mongo server
    protected DatabaseService() {
        mongoClient = new MongoClient("127.0.0.1", 27017);
    }

    //define mongo database
    protected MongoDatabase getDB() {
        return mongoClient.getDatabase("comp391_mingerso");
    }

    //define mongo collection for messages within database
    protected MongoCollection<Document> getMessageCollection() {
        return getDB().getCollection("chitterMessage");
    }

    //define mongo collection for users within database
    protected MongoCollection<Document> getUserCollection() {
        return getDB().getCollection("chitterUser");
    }


    //----------ID ALLOCATION -------------------------------------------------

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


    //----------TO AND FROM BSON------------------------------------------------

    //creates and returns BSON document from inputted user
    protected static Document userToBson(User u) {
        //sends each user session to the sessionToBson function and adds to a new document list
        List<Document> sessions = new ArrayList<>();
        for (Session s : u.getSessions()) {
            sessions.add(sessionToBson(s));
        }
        //returns a new BSON document with all user details
        return new Document("_id", new ObjectId(u.getId()))
                .append("email", u.getEmail())
                   .append("hash", u.getHash())
                .append("sessions", sessions);
    }

    //creates and returns a new BSON document from inputted session
    protected static Document sessionToBson(Session s) {
        return new Document("_id", new ObjectId(s.getId()))
                .append("ipAddress", s.getIpAddress())
                .append("since", s.getSince());
    }

    //creates and returns a new BSON document from inputted message
    protected static Document messageToBson(Message m) {
        return new Document("_id",new ObjectId(m.getId()))
                .append("userId", m.getUserId())
                .append("message", m.getMessage())
                .append("time", m.getTime())
                .append("tags", m.getTags());
    }

    //creates and returns a new user from inputted BSON document
    protected static User userFromBson(Document d) {
        // Checks if document is null
        if (d == null) {
            return null;
        }
        //extracts the user details and creates a new user
        String id = d.getObjectId("_id").toHexString();
        String email = d.getString("email");
        String hash = d.getString("hash");
        User u =  new User(id, email, hash);
        // Sends each BSON session to sessionFromBson function and puts into the new users session list
        List<Document> sessions = d.get("sessions", List.class);
        if(sessions != null) {
            for (Document sd : sessions) {
                Session s = sessionFromBson(sd);
                u.newUserSession(s.getId(), s);
            }
        }

        return u;
    }

    //creates and returns a session a new session from inputted BSON document
    protected static Session sessionFromBson(Document d) {
        // This lets us call this method even if d is null
        if (d == null) {
            return null;
        }
        //extracts the session details and creates a new session
        String id = d.getObjectId("_id").toHexString();
        String ip = d.getString("ipAddress");
        long since = d.getLong("since");
        return new Session(id, ip, since);
    }

    //creates and returns a new message from inputted BSON document
    protected static Message messageFromBson(Document d) {
        // This lets us call this method even if d is null
        if (d == null) {
            return null;
        }
        //extracts the message details from the BSON document and creates a new message
        String id = d.getObjectId("_id").toHexString();
        String userId = d.getString("userId");
        String message = d.getString("message");
        Long time = d.getLong("time");
        Message m =  new Message(id, userId, message, time);
        return m;
    }


    //---------MESSAGES--------------------------------------------------------

    //Sends inputted message to messageToBson and puts result in the message database
    public void storeMessage(Message message) {
        getMessageCollection().insertOne(messageToBson(message));
    }

    //Searches the message database for a string,
    //Determines whether search is a default search, user search or tag search
    //Returns an arraynode of the messages matching the string
    public ArrayNode searchMessages(String tag) {
        //Initialize the array to return
        ArrayNode array = Json.newArray();
        //Initialize a find iterable to iterate through a search request
        FindIterable <Document> myDoc;
        //if no search criteria (default search), sort and return all messages in database to limit of 30
        if (tag.equals("")){
            myDoc = getMessageCollection().find().sort(new Document("time", -1)).limit(30);
        }
        //if search begins with @, sort and return all messages where userId matches user with user email matching the string
        else if(tag.substring(0,1).equals("@")){
            //get userId of user with email matching the string less @ symbol
            String userId = getUserByEmail(tag.substring(1)).getId();
            //search message database for userId
            myDoc = getMessageCollection().find(new Document("userId", userId)).sort(new Document("time",-1)).limit(30);
        }
        //search for tags in message database, sort and return all messages in database to limit of 30
        else {
            myDoc = getMessageCollection().find(new Document("tags", new Document("$regex", tag))).sort(new Document("time", -1)).limit(30);
        }
        //Create a cursor on the finditerable to go through each result one by one
        MongoCursor<Document> cursor = myDoc.iterator();
        try {
            while (cursor.hasNext()) {
                //Sends the document to messageFromBson, extracts the message and puts into object node
                ObjectNode message = Json.newObject();
                Message m = messageFromBson(cursor.next());
                message.put("message", m.getMessage());
                message.put("user", m.getUserId());
                message.put("age", System.currentTimeMillis() - m.getTime());
                //get the user's email and put it in the objectnode
                message.put("email", getUserById(m.getUserId()).getEmail());
                //creates an arraynode of the tags and puts it into the objectnode
                ArrayNode tags = Json.newArray();
                for(String hashtag: m.getTags()){
                    ObjectNode entry = Json.newObject();
                    entry.put("tag",hashtag);
                    tags.add(entry);
                }
                message.put("tags", tags);
                //adds the objectnode to the arraynode
                array.add(message);
            }
        } finally {
            cursor.close();
        }
        return array;
    }


    //--------USERS-----------------------------------------------------------

    //stores a user in the database
    public User registerUser(User u) {
        getUserCollection().insertOne(userToBson(u));
        return u;
    }

    //Update the user entry in the database with the inputted user
    public void update(User u) {
        getUserCollection().replaceOne(new Document("_id", new ObjectId(u.getId())), userToBson(u));
    }

    //searches the user database for the inputted id string, returns the user as a user object
    public User getUserById(String id) {
        Document d = getUserCollection().find(new Document("_id", new ObjectId(id))).first();
        //send document to userFromBson to create user
        User u = userFromBson(d);
        if (u != null) {
            return u;
        } else {
            return null;
        }
    }

    //Get the user by email and password, returning null if they don't exist or the password is wrong
    public User getUserByEmail(String email, String password) {
        //search for first document with matching email
        Document d = getUserCollection().find(new Document("email", email)).first();
        //send document to userFromBson to create user
        User u = userFromBson(d);
        //check password angainst inputted password
        if (u != null && BCrypt.checkpw(password, u.getHash())) {
            return u;
        } else {
            return null;
        }
    }

    //Get the user by email, returning null if they don't exist
    public User getUserByEmail(String email) {
        //search for first document with matching email
        Document d = getUserCollection().find(new Document("email", email)).first();
        //send document to userFromBson to create user
        User u = userFromBson(d);
        if (u != null) {
            return u;
        } else {
            return null;
        }
    }

    //Get the user by sessionID, returning null if they don't exist
    public User getUserFromSession(String sessionId) {
        //search for first document with matching sessionId
        Document d = getUserCollection().find(new Document("sessions._id", new ObjectId(sessionId))).first();
        //send document to userFromBson to create user
        User u = userFromBson(d);
        if (u != null) {
            return u;
        } else {
            return null;
        }
    }
}
