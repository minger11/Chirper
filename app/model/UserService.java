package model;

import java.util.*;
/**
 * Created by mingerso on 30/07/15.
 */

//the userservice class
public class UserService {

    //creates a new instance of the userservice
    public static final UserService instance = new UserService();

    //the hashmap for storing all the users
    private static HashMap<String, User> datastrct = new HashMap<String, User>();

    //stores a user in the user hashmap
    public User registerUser(User u) {
        //need to eventually check that email doesnt exist yet
        String email = u.getEmail();
        datastrct.put(email, u);
        return u;
    }

    //returns a user based on the email address
    public User getUser(String email) {
        User u = (User)datastrct.get(email);
        return u;
    }

    //returns a user based on the sessionId
    public User getUserFromSession(String sessionId) {
        Iterator it = datastrct.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            User user = (User) pair.getValue();
            if(user.hm.get(sessionId)!=null){
                return user;
            }

        }
        return null;
    }

    //checks if a userexists in the database
    public static Boolean userExists(String email) {
        Iterator it = datastrct.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            User user = (User) pair.getValue();
            if(user.getEmail().equals(email)){return true;}
        }
        return false;
    }
}