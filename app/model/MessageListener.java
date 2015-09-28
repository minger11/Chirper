package model;

/**
 * A Single Abstract Method (SAM) interface for things that want to hear about messages
 */
public interface MessageListener {

    public void receiveMessage(Message g);

}
