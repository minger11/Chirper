package model;

import java.util.*;
import java.util.regex.*;
/**
 * Created by mingerso on 4/08/15.
 */

//the message class
public class Message {
    String message;
    String userId;
    long time;
    String id;
    List <String> tags;

    //constructor for a new mesage
    //takes in a user and a string
    //sets the time and an id
    //creates an arraylist of the tags which are found through a regex
    public Message(String id, String userId, String message){
        this.time = System.currentTimeMillis();
        this.message = message;
        this.userId = userId;
        this.id = id;
        this.tags = new ArrayList<String>();
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(message);
        while(matcher.find()){
            this.tags.add(matcher.group(1));
        }
    }

    //returns the arraylist of tags
    public List<String> getTags() { return this.tags; }

    //returns the userId
    public String getUserId() { return this.userId; }

    //returns the time
    public long getTime() {
        return this.time;
    }

    //returns the message
    public String getMessage() { return this.message; }

    //returns the id
    public String getId() { return this.id; }
}

