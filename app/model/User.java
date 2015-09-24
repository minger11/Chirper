package model;

import java.util.*;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by mingerso on 30/07/15.
 */

//the user class
public class User{

    //creates a hashmap for the sessions to ids
    ConcurrentHashMap<String, Session> activeSessions = new ConcurrentHashMap<>();

    String id;

    String email;

    String hash;

    //creates a new user based on an email, id and hash
    public User(String id, String email, String hash) {
        this.id = id;
        this.email = email;
        this.hash = hash;
    }

    //stores a sessionId and session in the users session hashmap
    public void newUserSession(String currentSessionId, Session currentSession){
        activeSessions.put(currentSessionId, currentSession);
    }

    //removes a session from the users session hashmap
    public void removeUserSession(String currentSessionId){
        activeSessions.remove(currentSessionId);
    }

    //gets the id
    public String getId() {
        return this.id;
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
    public ConcurrentHashMap<String, Session> getActiveSessions() {return this.activeSessions; }

    /**
     * Is a particular session active on this user?
     */
    public boolean hasSession(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    public Session[] getSessions() {
        Collection<Session> values = activeSessions.values();
        return values.toArray(new Session[values.size()]);
    }
}
