package model;

import java.util.*;
/**
 * Created by mingerso on 30/07/15.
 */

//the user class
public class User{

    //creates a hashmap for the sessions to usernames
    public HashMap<String, Session> hm = new HashMap<String, Session>();

    String username;

    String email;

    String hash;

    //creates a new user based on an email, username and hash
    public User(String email, String username, String hash) {
        this.email = email;
        this.username = username;
        this.hash = hash;
    }

    //stores a sessionId and session in the users session hashmap
    public void newUserSession(String currentSessionId, Session currentSession){
        hm.put(currentSessionId, currentSession);
    }

    //removes a session from the users session hashmap
    public void removeUserSession(String currentSessionId){
        hm.remove(currentSessionId);
    }

    //sets the users username
    public void setUsername(String newUsername){
        this.username = newUsername;
    }

    //gets the username
    public String getUsername() {
        return this.username;
    }

    //returns the users email
    public String getEmail() {
        return this.email;
    }

    //returns the users hash
    public String getHash() {
        return this.hash;
    }

    //returns the users session hashmap
    public HashMap<String, Session> getMap() {return this.hm; }
}
