package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonNode.*;
import model.*;
import play.mvc.*;
import play.mvc.BodyParser.*;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Http.RequestBody.*;
import play.mvc.Result;
import play.mvc.WebSocket;

import java.util.*;

/**
 * Created by mingerso on 31/07/15.
 */

public class Application extends Controller {

    //database of the messagestore
    protected static DatabaseService database = DatabaseService.instance;

    //---------CURRENT USER RELATED-------------

    public final static String sessionVar = "My_session";

    //returns the session id of the present session and creates one if none exists
    public static String getSessionId(){
        String id = session(sessionVar);
        if(!database.isValidId(id)) {
            id = database.allocateId();
            session(sessionVar, id);
        }
        return id;
    }

    //returns the user that is presently logged in
    public static User getExistingUser(){
        //get user from database based on the current session
        User existingUser = Application.database.getUserFromSession(getSessionId());
        return existingUser;
    }

    //returns true if the current session is associated with a user
    public static boolean isLoggedIn(){
        if(getExistingUser()!=null) return true;
        return false;
    }

    //---------PAGE DIRECTIONS-------------------------

    //if logged in, sends user to the viewsessions page with a hashmap of the users activesessions
    public static Result sessions() {
        if(isLoggedIn()) {
            return ok(views.html.application.sessions.render(null, Application.database.getUserFromSession(getSessionId())));
        } else {
            //redirects user if not logged in
            return redirect("/");}
    }

    //sends the existing user and their messages to index page
    public static Result index() {

        return ok(views.html.application.index.render(null, getExistingUser()));
    }

    //sends user to register page
    public static Result register(){

        return ok(views.html.application.register.render(null, getExistingUser()));
    }

    //sends the existing user and their messages to spa page
    public static Result spa() {

        return ok(views.html.application.spa.render(getExistingUser()));
    }

    //---------XMLHttp AND WEBSOCKET RELATED-----------------------------

    //takes a string as a parameter, sets CROS headers and sends string to messagesFromStore
    public static Result getMessage(String text) {
        //Sets CORS header and returns array node of search results
        response().setHeader("Access-Control-Allow-Origin", "*");
        return messagesFromStore(text);
    }

    //Sets CORS headers for the cross origin post request
    public static Result optionsMessage() {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setHeader("Access-Control-Allow-Credentials", "true");
        response().setHeader("Access-Control-Allow-Headers", "content-type");
        return ok("");
    }

    //parses post data received from XMLHttp, sends text to storemessage function and sets CORS headers
    public static Result postMessage() {
        if(isLoggedIn()) {
            //extract the posted message
            JsonNode json = request().body().asJson();
            //if the message is null, send message
            if (json == null) {
                return ok("Expecting Json data");
            } else {
                //parse the message text to string
                //send text string to create message and store in database
                storeMessage(json.findPath("message").textValue());
                //set CORS header to allow post to be made
                response().setHeader("Access-Control-Allow-Origin", "*");
                return ok("");
            }
        } else return ok("Not logged in!");
    }

    //Websocket end point
    public static WebSocket<String> socket(String topic) {
        return WebSocket.<String>withActor((out) -> MessageWebSocketActor.props(topic, out));
    }

    //---------LOGIN AND REGISTRATION MANAGEMENT------------------------

    //extracts entered emails and passwords, creates user in database
    //produces various error messages if problems exist in the registration
    public static Result doRegister(){
        //extract email and hashed password entered
        String email = request().body().asFormUrlEncoded().getOrDefault("email", new String[0])[0];
        String email2 = request().body().asFormUrlEncoded().getOrDefault("email2", new String[0])[0];
        String hash = BCrypter.encrypt(request().body().asFormUrlEncoded().getOrDefault("password", new String[0])[0]);
        //check if user is already registered in database
        if(Application.database.getUserByEmail(email)!=null) {
            return ok(views.html.application.register.render("Email already registered. Please try again", getExistingUser()));
        }
        //check that the emails entered are equal
        if(email.equals(email2)) {
            //check if passwords are equal
            if (BCrypter.checkpassword(request().body().asFormUrlEncoded().getOrDefault("password2", new String[0])[0], hash)) {
                //create user, store in database and redirect to spa
                User person = new User(database.allocateId(), email, hash);
                Application.database.registerUser(person);
                return ok(views.html.application.index.render("Account created!", getExistingUser()));
            } else {
                return ok(views.html.application.register.render("Passwords didn't match. Please try again", getExistingUser()));
            }
        } else {
            return ok(views.html.application.register.render("Emails didn't match. Please try again", getExistingUser()));
        }
    }

    //takes a username and password, checks against the database and logs user in
    //removes existing session if one exists and produces various error messages for errors in the login
    public static Result doLogin(){
        //extract email and password as entered by user
        String email = request().body().asFormUrlEncoded().getOrDefault("email", new String[0])[0];
        String password = request().body().asFormUrlEncoded().getOrDefault("password", new String[0])[0];
        //fetch the user from the database
        User u = database.getUserByEmail(email, password);
        if(u!=null) {
                    //if user is already logged in, removes user session if login user is different
                    if (getExistingUser() != null) {
                        if (getExistingUser().getEmail().equals(email)) {
                            return ok(views.html.application.index.render(email + " is already logged in", getExistingUser()));
                        } else {
                            getExistingUser().removeUserSession(getSessionId());
                        }
                    }
                    //creates a new session, updates the database and redirects the user to spa
                    Session currentSession = new Session(getSessionId(), request().remoteAddress(), System.currentTimeMillis());
                    u.newUserSession(getSessionId(), currentSession);
                    database.update(u);
                    return redirect("/spa");
        }
        return ok(views.html.application.index.render("The Email or Password entered did not match our records. Please try again", getExistingUser()));
    }

    //removes the user from the current session and returns the user to the index page
    public static Result doLogOut(){
        if(isLoggedIn()) {
            //get user of current session
            User u = Application.database.getUserFromSession(getSessionId());
            //remove session from user in database
            u.removeUserSession(getSessionId());
            database.update(u);
        }
        //redirect user to spa
        return redirect("/");
    }

    //extracts the selected remote session and remotely logs the session out
    public static Result remoteLogout(){
        if(isLoggedIn()) {
            //Define remote session selected by user
            String sessionId = request().body().asFormUrlEncoded().getOrDefault("sessionId", new String[0])[0];
            //get user of current session
            User u = Application.database.getUserFromSession(getSessionId());
            if(u!=null) {
                //if the current user matches the requested logout
                if (u.getId().equals(getExistingUser().getId())) {
                    //remove remote session from user
                    u.removeUserSession(sessionId);
                    //update the database
                    database.update(u);
                    //if user logged current session out, redirect to spa. Otherwise refresh page.
                    if (getExistingUser() != null) {
                        return ok(views.html.application.sessions.render(("logged out of session " + sessionId), getExistingUser()));
                    } else {
                        return redirect("/");
                    }
                }
            }
        }
        return redirect("/");
    }

    //--------GET AND POST MESSAGES-------------------------------------

    //takes a text string, creates a new message, stores in the database and alerts the socket listeners at message hub
    public static void storeMessage(String text){
            //get current time
            long time = System.currentTimeMillis();
            //create a new message using the text, current user and current time
            Message message = new Message(database.allocateId(), getExistingUser().getId(), text, time);
            //database message in database
            Application.database.storeMessage(message);
            //alert hub that a new message has been created
            MessageHub.getInstance().send(message);
            return;
    }

    //Sends the input string to be searched in the database
    public static Result messagesFromStore(String text){
        if(isLoggedIn()) {
            return ok(Application.database.searchMessages(text));
        } else return ok("Not logged in!");
    }

    //--------APIs------------------------------------------------------

    //returns a json object of all messages for the user who has the email given as a parameter
    public static Result userMessages(String email){
        String searchString = "@"+email;
        return messagesFromStore(searchString);
    }

    //accepts plain text input and sends the text to the storeMessage function
    public static Result postAPIMessage() {
        RequestBody body = request().body();
        String text = body.asText();
        if(isLoggedIn()) {
            if (text != "") {
                storeMessage(text);
                return ok("Message posted!");
            } else {
                return ok("You have to write something first!");
            }
        } else {
            return ok("Login to post!");
    }
    }
}





























