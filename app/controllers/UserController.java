package controllers;

import model.*;

import java.util.*;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.BodyParser.*;
import play.mvc.Http.RequestBody;
import play.mvc.Http.RequestBody.*;
import play.mvc.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonNode.*;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Created by mingerso on 31/07/15.
 */

public class UserController extends Controller {

    //instance of the user service
    protected static UserService instance = UserService.instance;

    //instance of the messagestore
    protected static MessageStore store = MessageStore.instance;

    public final static String sessionVar = "My_session";

    //returns the session id of the present session and creates one if none exists
    public static String getSessionId(){
        String id = session(sessionVar);
        if(!instance.isValidId(id)) {
            id = instance.allocateId();
            session(sessionVar, id);
        }
        return id;
    }

    //returns the user that is presently logged in
    public static User getExistingUser(){
        User existingUser = UserController.instance.getUserFromSession(getSessionId());
        return existingUser;
    }

    //sends the existing user and their messages to index page
    public static Result index() {
            return ok(views.html.application.index.render(null,getExistingUser(), UserController.store.getMessageStore()));
    }

    //checks the two emails and passwords against each other
    //returns to page if no match or if email exists in database
    //if successful, sends user to index with success message
    public static Result doRegister(){
        String email = request().body().asFormUrlEncoded().getOrDefault("email", new String[0])[0];
        String email2 = request().body().asFormUrlEncoded().getOrDefault("email2", new String[0])[0];
        String hash = BCrypter.encrypt(request().body().asFormUrlEncoded().getOrDefault("password", new String[0])[0]);
        if(UserController.instance.userExists(email)) {
            return ok(views.html.application.register.render("Email already registered. Please try again", getExistingUser()));
        }
        if(email.equals(email2)) {
            if (BCrypter.checkpassword(request().body().asFormUrlEncoded().getOrDefault("password2", new String[0])[0], hash)) {
                User person = new User(instance.allocateId(), email, hash);
                UserController.instance.registerUser(person);
                return ok(views.html.application.login.render("Account successfully created! Please login.", getExistingUser()));
            } else {
                return ok(views.html.application.register.render("Passwords didn't match. Please try again", getExistingUser()));
            }
        } else {
            return ok(views.html.application.register.render("Emails didn't match. Please try again", getExistingUser()));
        }
    }

    //Retrieves the email and password from the post
    //checks if the email is registered and returns user to login page with error message if it is not
    //checks password and sends user to default index if user exists and password is correct
    //removes session if new login came from a previous session of a different user
    //returns user to login page with error message if logged in user tries to login
    public static Result doLogin(){
        String email = request().body().asFormUrlEncoded().getOrDefault("email", new String[0])[0];
        String password = request().body().asFormUrlEncoded().getOrDefault("password", new String[0])[0];

        User u = instance.getUser(email, password);

        //check if logged in as someone else
        if(UserController.instance.userExists(email)) {
            if (getExistingUser() != null) {
                if (getExistingUser().getEmail().equals(email)) {
                    return ok(views.html.application.login.render(email + " is already logged in", getExistingUser()));
                } else {
                    getExistingUser().removeUserSession(getSessionId());
                }
            }

            //check password and login
            if (BCrypter.checkpassword(password, u.getHash())) {
                Session currentSession = new Session(getSessionId(), request().remoteAddress(), System.currentTimeMillis());
                u.newUserSession(getSessionId(), currentSession);
                instance.update(u);
                return index();
            } else {
                return ok(views.html.application.login.render("Incorrect password, please try again", getExistingUser()));
            }
        }
        return ok(views.html.application.login.render("We don't have an account registered for " +email+ ". Please try again", getExistingUser()));
    }

    //sends user to register page
    public static Result registerForm(){
        return ok(views.html.application.register.render(null, getExistingUser()));
    }

    //sends user to login page
    public static Result loginForm(){
            return ok(views.html.application.login.render(null, getExistingUser()));
    }

    //removes the user from the current session and returns the user to the default index
    public static Result doLogOut(){
        //get user of current session
        User u = UserController.instance.getUserFromSession(getSessionId());
        //remove session from user
        u.removeUserSession(getSessionId());
        //update the database
        instance.update(u);
        return index();
    }

    //sends user to the viewsessions page with a hashmap of the users activesessions
    public static Result viewSessionsForm() {
        return ok(views.html.application.activesessions.render(null, UserController.instance.getUserFromSession(getSessionId())));
    }

    //removes session selected
    //checks if current session is valid
    //if valid, returns user to active sessions with a success message
    //if current session isnt valid, returns user to index
    public static Result remoteLogout(){
        //Define remote session selected by user
        String sessionId = request().body().asFormUrlEncoded().getOrDefault("sessionId", new String[0])[0];
        //get user of current session
        User u = UserController.instance.getUserFromSession(getSessionId());
        //remove session from user
        u.removeUserSession(sessionId);
        //update the database
        instance.update(u);
        if(getExistingUser()!=null){
            return ok(views.html.application.activesessions.render(("logged out of session "+sessionId),getExistingUser()));
        } else {
            return index();
        }
    }

    //takes in text from the post
    //checks if no text present and returns an error message if so
    //otherwise creates a new message, puts the message in the message store
    //and returns the user to the index with a success message
    public static Result doPost() {
        String text = request().body().asFormUrlEncoded().getOrDefault("message", new String[0])[0];
        if(text!="") {
            Message message = new Message(instance.allocateId(), getExistingUser().getId(), text);
            UserController.store.storeMessage(message);
            return ok(views.html.application.index.render("Message posted!",getExistingUser(), UserController.store.getMessageStore()));
        } else {
            return ok(views.html.application.index.render("You have to write something first!", getExistingUser(), UserController.store.getMessageStore()));
        }
    }

    //takes in an email string as an argument
    //fetches the user and passes it to the user page
    public static Result user(String email){
        User user = UserController.instance.getUser(email);
        return ok(views.html.application.index.render(null, getExistingUser(), UserController.store.getUserMessages(user)));
    }

    //searches for the parameter string and returns a hashmap of all messages containing the string to the search page
    public static Result search(String search){
        return ok(views.html.application.search.render(null,getExistingUser(),UserController.store.getTaggedMessages(search)));
    }

    //returns a json object of all messages for the user who has the email given as a parameter
    public static Result userMessages(String email){
        ObjectNode result = Json.newObject();
        User user = instance.getUser(email);
        HashMap<String,Message> userMessages = UserController.store.getUserMessages(user);
        Iterator it = userMessages.entrySet().iterator();
        if(!it.hasNext())return ok("No messages to show");
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Message message = (Message) pair.getValue();
            String text = message.getMessage();
            String id = message.getId();
            result.put(id,text);
            }
        return ok(result);
    }

    //returns a json object of all messages which have the exact tag as given in the parameter
    public static Result taggedMessages(String tag){
        ObjectNode result = Json.newObject();
        HashMap<String,Message> userMessages = UserController.store.getExactTaggedMessages(tag);
        Iterator it = userMessages.entrySet().iterator();
        if(!it.hasNext())return ok("The requested tag has never been used");
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Message message = (Message) pair.getValue();
            String text = message.getMessage();
            String id = message.getId();
            result.put(id,text);
        }
        return ok(result);
    }

    //accepts plain text input and posts the text as a message to the current users message hashmap
    public static Result postMessage() {
        RequestBody body = request().body();
        String text = body.asText();
        if(getExistingUser()!=null) {
            if (text != "") {
                Message message = new Message(instance.allocateId(), getExistingUser().getId(), text);
                UserController.store.storeMessage(message);
                return ok("Message posted!");
            } else {
                return ok("You have to write something first!");
            }
        } else {
            return ok("Login to post!");
    }
    }
}





























