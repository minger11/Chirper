package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a simple publish-subscribe list. It maintains a list of listeners, and whenever it receives a call to
 * <code>send</code>, it calls <code>receiveMessage</code> on every registered listener.
 */
public class MessageHub {

    List<MessageListener> listeners;

    static final MessageHub instance = new MessageHub();

    public static MessageHub getInstance() {
        return instance;
    }

    protected MessageHub() {
        this.listeners = Collections.synchronizedList(new ArrayList<>());
    }

    public void send(Message g) {
        for (MessageListener listener : listeners) {
            listener.receiveMessage(g);
        }
    }

    public void addListener(MessageListener l) {
        this.listeners.add(l);
    }

    public void removeListener(MessageListener l) {
        this.listeners.remove(l);
    }

}
