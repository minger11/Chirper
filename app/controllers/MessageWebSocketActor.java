package controllers;

import akka.actor.*;
import model.MessageHub;
import model.MessageListener;

public class MessageWebSocketActor extends UntypedActor {

    /**
     * We don't create the actor ourselves. Instead, Play will ask Akka to make it for us. We have to give Akka a
     * "Props" object that tells Akka what kind of actor to create, and what constructor arguments to pass to it.
     * This method produces that Props object.
     */
    public static Props props(String topic, ActorRef out) {
        // Create a Props object that says:
        // - I want a MessageWebSocketActor,
        // - and pass (topic, out) as the arguments to its constructor
        return Props.create(MessageWebSocketActor.class, topic, out);
    }

    /** The Actor for the client (browser) */
    private final ActorRef out;

    /** The topic string we have subscribed to */
    private final String topic;

    /** A listener that we will register with our MessageHub */
    private final MessageListener listener;

    /**
     * This constructor is called by Akka to create our actor (we don't call it ourselves).
     */
    public MessageWebSocketActor(String topic, ActorRef out) {
        this.topic = topic;
        this.out = out;

        /*
          Our MessageListener, written as a Java 8 Lambda.
          Whenever we receive a gibberish, convert it to a JSON string, and send it to the client.
         */
        this.listener = (g) -> {
           // String message = Application.toJson(g).toString();
            String message = Application.database.searchMessages(this.topic).toString();
                /*
                 This asynchronously sends the message to the WebSocket client.
                 Self is a reference to this actor (the sender)
                 */
            out.tell(message, self());
        };

        // Register this actor to hear gibberish
        MessageHub.getInstance().addListener(listener);
    }

    /**
     * This is called whenever the browser sends a message to the serverover the websocket
     */
    public void onReceive(Object message) throws Exception {
        // The client isn't going to send us messages down the websocket in this example, so this doesn't matter
        // DOESNT DO ANYTHING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if (message instanceof String) {
            out.tell("I received your message: " + message, self());
        }
    }

    /**
     * This is called by Play after the WebSocket has closed
     */
    public void postStop() throws Exception {
        // De-register our listener
        MessageHub.getInstance().removeListener(this.listener);
    }
}