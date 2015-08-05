package model;

import java.util.*;
import java.util.regex.*;
/**
 * Created by mingerso on 4/08/15.
 */

//the message store class
public class MessageStore {

    //creates a new instance of the messagestore
    public static final MessageStore instance = new MessageStore();

    //the hashmap for the message store
    private static HashMap<String, Message> messageStore = new HashMap<String, Message>();

    //puts a new message in the message store
    public void storeMessage(Message message) {
        messageStore.put(message.getId(), message);
    }

    //returns a hashmap of all messages for the given user
    public HashMap<String, Message> getUserMessages(User user){
        HashMap<String, Message> userMessages = new HashMap<String, Message>();
        Iterator it = messageStore.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Message message = (Message) pair.getValue();
            if(message.getUser().equals(user)){
                userMessages.put(message.getId(), message);
            }
        }
        return userMessages;
    }

    //returns the hashmap of the messagestore
    public HashMap<String, Message> getMessageStore(){
        return messageStore;
    }

    //returns a hashmap of all messages with a hashtag containing the given string
    public HashMap<String, Message> getTaggedMessages(String tag){
        HashMap<String, Message> getTaggedMessages = new HashMap<String, Message>();
        Iterator it = messageStore.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Message message = (Message) pair.getValue();
            Iterator<String> it2 = message.getTags().iterator();
            while(it2.hasNext()){
                if (Pattern.compile(tag).matcher(it2.next()).find()) {
                    getTaggedMessages.put(message.getId(), message);
                }
            }
        }
        return getTaggedMessages;
    }

    //returns a hashmap of all messages with a hashtag that is exactly the string
    public HashMap<String, Message> getExactTaggedMessages(String tag){
        HashMap<String, Message> getTaggedMessages = new HashMap<String, Message>();
        Iterator it = messageStore.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Message message = (Message) pair.getValue();
            Iterator<String> it2 = message.getTags().iterator();
            while(it2.hasNext()){
                if (Pattern.compile(tag).matcher(it2.next()).matches()) {
                    getTaggedMessages.put(message.getId(), message);
                }
            }
        }
        return getTaggedMessages;
    }

}
