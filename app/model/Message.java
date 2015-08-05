package model;

import java.util.*;
import java.util.regex.*;
/**
 * Created by mingerso on 4/08/15.
 */

//the message class
public class Message {
    String message;
    User user;
    long time;
    String id;
    List <String> tags;

    //constructor for a new mesage
    //takes in a user and a string
    //sets the time and an id
    //creates an arraylist of the tags which are found through a regex
    public Message(User user, String message){
        this.time = System.currentTimeMillis();
        this.message = message;
        this.user = user;
        this.id = Long.toString(System.currentTimeMillis());
        this.tags = new ArrayList<String>();
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(message);
        while(matcher.find()){
            this.tags.add(matcher.group(1));
        }
    }

    //returns the arraylist of tags
    public List<String> getTags() { return this.tags; }

    //returns the user
    public User getUser() { return this.user; }

    //returns the time
    public long getTime() {
        return this.time;
    }

    //returns the message
    public String getMessage() { return this.message; }

    //returns the id
    public String getId() { return this.id; }
}

